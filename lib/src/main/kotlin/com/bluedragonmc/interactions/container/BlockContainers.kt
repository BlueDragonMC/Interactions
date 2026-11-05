package com.bluedragonmc.interactions.container

import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.blockCenter
import com.bluedragonmc.interactions.util.getBlockOrAir
import com.bluedragonmc.interactions.util.playBlockSound
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.entity.Player
import net.minestom.server.event.inventory.InventoryItemChangeEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.sound.SoundEvent
import net.minestom.server.tag.Tag
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom

/**
 * A Minestom [Inventory] whose contents are stored on the block entity at [blockPosition].
 *
 * Contents are kept in sync with the `Items` tag on the block.
 */
class BlockContainerInventory(
    val instance: Instance,
    val blockPosition: BlockVec,
    private val blockKey: Key,
    type: InventoryType,
    title: Component,
    private val openSound: SoundEvent?,
    private val closeSound: SoundEvent?,
    private val animation: ContainerAnimation,
) : Inventory(type, title) {

    /** True while [reload] is copying items in, to avoid writing them straight back out. */
    private var loading = false

    init {
        eventNode().addListener(InventoryItemChangeEvent::class.java) {
            if (!loading) persist()
        }
    }

    /** Replaces this inventory's contents with the items currently stored on [block]. */
    fun reload(block: Block) {
        loading = true
        try {
            for (slot in 0 until size) {
                setItemStack(slot, readContainerItem(block, slot), false)
            }
        } finally {
            loading = false
        }
    }

    /** Writes this inventory's contents back to the block, if the block is still the same container. */
    fun persist() {
        val block = instance.getBlockOrAir(blockPosition)
        if (block.key() != blockKey) return
        instance.setBlock(blockPosition, block.withContainerItems(getItemStacks().toList()))
    }

    override fun addViewer(player: Player): Boolean {
        val wasEmpty = viewers.isEmpty()
        val added = super.addViewer(player)
        if (added && wasEmpty) {
            playSound(openSound)
            animate(true)
        }
        return added
    }

    override fun removeViewer(player: Player): Boolean {
        val removed = super.removeViewer(player)
        if (removed && viewers.isEmpty()) {
            persist()
            playSound(closeSound)
            animate(false)
            BlockContainers.release(this)
        }
        return removed
    }

    /** Tells clients to play this container's open or close animation. */
    private fun animate(open: Boolean) {
        when (animation) {
            ContainerAnimation.NONE -> {}
            ContainerAnimation.LID -> {
                if (instance.isChunkLoaded(blockPosition)) {
                    instance.sendBlockAction(blockPosition, LID_ACTION, if (open) 1 else 0)
                }
            }
            ContainerAnimation.BLOCK_STATE -> {
                val block = instance.getBlockOrAir(blockPosition)
                if (block.key() != blockKey || block.getProperty(States.OPEN) == null) return
                instance.setBlock(blockPosition, block.withProperty(States.OPEN, open.toString()))
            }
        }
    }

    private fun playSound(sound: SoundEvent?) {
        if (sound == null) return
        instance.playBlockSound(
            sound,
            blockPosition.blockCenter(),
            volume = 0.5f,
            pitch = ThreadLocalRandom.current().nextFloat() * 0.1f + 0.9f,
        )
    }

    private companion object {
        /**
         * The block action both chests and shulker boxes use to set their open count; the client
         * animates the lid towards whichever state this signals.
         */
        const val LID_ACTION: Byte = 1
    }
}

/**
 * Keeps a single [BlockContainerInventory] alive per open block, so that two players looking at the
 * same container share one inventory (and thus see each other's changes).
 */
internal object BlockContainers {
    private val open = ConcurrentHashMap<ContainerKey, BlockContainerInventory>()

    /**
     * Opens the container at [position] for [player], creating and loading its inventory the first
     * time it is opened. [defaultTitle] is used when the block has no custom name, and [openSound] /
     * [closeSound] are played when the container opens and closes.
     */
    fun open(
        player: Player,
        instance: Instance,
        position: BlockVec,
        type: InventoryType,
        defaultTitle: Component,
        openSound: SoundEvent?,
        closeSound: SoundEvent?,
        animation: ContainerAnimation,
    ) {
        val block = instance.getBlockOrAir(position)
        val key = ContainerKey(instance, position)
        val inventory = open[key] ?: BlockContainerInventory(
            instance, position, block.key(), type, title(block, defaultTitle), openSound, closeSound, animation,
        ).also {
            it.reload(block)
            open[key] = it
        }
        player.openInventory(inventory)
    }

    /** Closes every viewer of the container at [position], releasing its inventory. */
    fun close(instance: Instance, position: BlockVec) {
        val inventory = open[ContainerKey(instance, position)] ?: return
        inventory.viewers.toList().forEach { it.closeInventory() }
    }

    fun release(inventory: BlockContainerInventory) {
        open.remove(ContainerKey(inventory.instance, inventory.blockPosition), inventory)
    }

    private fun title(block: Block, fallback: Component): Component =
        Tag.Component("CustomName").read(block.nbtOrEmpty()) ?: fallback

    private data class ContainerKey(val instance: Instance, val position: BlockVec)
}
