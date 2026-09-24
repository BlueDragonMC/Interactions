package com.bluedragonmc.interactions.rules.interaction.misc

import com.bluedragonmc.interactions.BlockInteractionRule
import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.container.BlockContainers
import com.bluedragonmc.interactions.container.readContainerItems
import com.bluedragonmc.interactions.plus
import com.bluedragonmc.interactions.util.Tags
import com.bluedragonmc.interactions.util.dropItemStack
import com.bluedragonmc.interactions.util.isSecondaryUseActive
import net.kyori.adventure.text.Component
import net.minestom.server.instance.block.Block
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack

/**
 * Opens a storage menu when one of the simple containers below is right-clicked, backed by the
 * block's own block-entity data so the contents are saved with the world.
 * Breaking a container drops whatever it holds.
 */
class ContainerInteractionRule : BlockInteractionRule(SIMPLE_CONTAINERS) {
    init {
        onInteract {
            if (isSecondaryUseActive()) return@onInteract
            val spec = specFor(block) ?: return@onInteract
            isBlockingItemUse = true
            BlockContainers.open(player, instance, blockPosition, spec.type, spec.title)
        }

        onDestroy {
            val spec = specFor(block) ?: return@onDestroy
            val items = readContainerItems(block, spec.type.size)
            BlockContainers.close(instance, blockPosition)
            items.filterNot(ItemStack::isAir).forEach { instance.dropItemStack(blockPosition, it) }
        }
    }

    private data class ContainerSpec(val type: InventoryType, val title: Component)

    private companion object {
        /** Chests (including trapped and copper variants), but not the ender chest. */
        val CHESTS: BlockSet = BlockSet.matching {
            it == "minecraft:chest" || (it.endsWith("_chest") && it != "minecraft:ender_chest")
        }

        val SIMPLE_CONTAINERS: BlockSet = CHESTS +
            BlockSet.fromTag("minecraft:shulker_boxes") +
            BlockSet.from(Block.BARREL, Block.DISPENSER, Block.DROPPER, Block.HOPPER)

        fun specFor(block: Block): ContainerSpec? = when {
            block.key() in Tags.SHULKER_BOXES ->
                ContainerSpec(InventoryType.SHULKER_BOX, Component.translatable("container.shulkerBox"))
            block.key() in CHESTS.keys ->
                ContainerSpec(InventoryType.CHEST_3_ROW, Component.translatable("container.chest"))
            block.key() == Block.BARREL.key() ->
                ContainerSpec(InventoryType.CHEST_3_ROW, Component.translatable("container.barrel"))
            block.key() == Block.DISPENSER.key() ->
                ContainerSpec(InventoryType.WINDOW_3X3, Component.translatable("container.dispenser"))
            block.key() == Block.DROPPER.key() ->
                ContainerSpec(InventoryType.WINDOW_3X3, Component.translatable("container.dropper"))
            block.key() == Block.HOPPER.key() ->
                ContainerSpec(InventoryType.HOPPER, Component.translatable("container.hopper"))
            else -> null
        }
    }
}
