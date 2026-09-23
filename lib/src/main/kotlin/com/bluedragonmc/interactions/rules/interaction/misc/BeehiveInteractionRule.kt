package com.bluedragonmc.interactions.rules.interaction.misc

import com.bluedragonmc.interactions.BlockInteractionRule
import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.util.*
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.sound.SoundEvent

/**
 * Full beehives can be sheared for honeycomb or collected with a glass bottle.
 */
class BeehiveInteractionRule : BlockInteractionRule(BlockSet.from(Block.BEEHIVE, Block.BEE_NEST)) {
    init {
        onInteract {
            if (isSecondaryUseActive()) return@onInteract
            val honeyLevel = block.getProperty(States.HONEY_LEVEL)?.toIntOrNull() ?: 0
            val material = heldMaterial
            if (honeyLevel < 5 || (material != Material.SHEARS && material != Material.GLASS_BOTTLE)) return@onInteract
            isBlockingItemUse = true

            if (material == Material.SHEARS) {
                instance.dropItemStack(blockPosition, ItemStack.of(Material.HONEYCOMB, 3))
                damageHeldItem(1)
                instance.playBlockSound(SoundEvent.BLOCK_BEEHIVE_SHEAR, blockPosition.blockCenter())
            } else {
                giveHoneyBottle(this)
                instance.playBlockSound(SoundEvent.ITEM_BOTTLE_FILL, blockPosition.blockCenter())
            }

            instance.setBlock(blockPosition, block.withProperty(States.HONEY_LEVEL, "0"))
        }
    }

    private fun giveHoneyBottle(event: PlayerBlockInteractEvent) {
        event.setHeldItem(event.heldItem.consume(1))
        event.player.giveOrDrop(event.instance, event.blockPosition, ItemStack.of(Material.HONEY_BOTTLE))
    }
}
