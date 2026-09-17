package com.bluedragonmc.interactions.rules.interaction

import com.bluedragonmc.interactions.InteractionRule
import com.bluedragonmc.interactions.util.*
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.utils.block.BlockIterator

/**
 * Bucket interactions.
 *
 * Filled buckets place a fluid source: water fills a waterloggable clicked block (unless the player is
 * sneaking), otherwise the fluid goes into the adjacent block. Empty buckets pick up water and lava
 * source blocks, and water from waterlogged blocks.
 */
class BucketRule : InteractionRule() {
    init {
        onInteract {
            if (isBlockingItemUse) return@onInteract
            when (heldMaterial) {
                Material.BUCKET ->
                    if (pickUpFluid(player, instance, hand, block, blockPosition)) isBlockingItemUse = true
                Material.WATER_BUCKET -> placeFluid(Block.WATER)
                Material.LAVA_BUCKET -> placeFluid(Block.LAVA)
                else -> return@onInteract
            }
        }

        // Empty buckets fill through the item-use path because the client does not report fluid
        // blocks as a target, so a ray from the player's eyes is used to find the fluid instead.
        onUseItem {
            if (itemStack.material() != Material.BUCKET) return@onUseItem
            val instance = player.instance ?: return@onUseItem
            val reach = player.getAttributeValue(Attribute.BLOCK_INTERACTION_RANGE)
            val ray = BlockIterator(player.position.asVec(), player.position.direction(), player.eyeHeight, reach)
            while (ray.hasNext()) {
                val position = ray.next()
                val targetBlock = instance.getBlockOrAir(position)
                if (targetBlock.air()) continue
                if (pickUpFluid(player, instance, hand, targetBlock, position)) isCancelled = true
                break
            }
        }
    }
}

private fun PlayerBlockInteractEvent.placeFluid(fluid: Block) {
    val waterlogging = fluid.key() == Block.WATER.key() &&
        !isSecondaryUseActive() &&
        block.getProperty(States.WATERLOGGED) == "false"
    if (waterlogging) {
        instance.setBlock(blockPosition, block.withProperty(States.WATERLOGGED, "true"))
    } else {
        // The fluid goes into the adjacent block, except that a water bucket fills a waterloggable
        // clicked block in place.
        val target = blockPosition.relative(blockFace)
        val targetBlock = instance.getBlockOrAir(target)
        if (!targetBlock.air() && !targetBlock.replaceable()) return
        instance.setBlock(target, fluid)
    }

    if (player.gameMode != GameMode.CREATIVE) setHeldItem(ItemStack.of(Material.BUCKET))
    isBlockingItemUse = true
}

/** Picks up the fluid [block] at [position], returning whether it produced a filled bucket. */
private fun pickUpFluid(player: Player, instance: Instance, hand: PlayerHand, block: Block, position: Point): Boolean {
    val filled = when {
        block.key() == Block.WATER.key() && block.getProperty(States.LEVEL) == "0" -> {
            instance.setBlock(position, Block.AIR)
            Material.WATER_BUCKET
        }
        block.key() == Block.LAVA.key() && block.getProperty(States.LEVEL) == "0" -> {
            instance.setBlock(position, Block.AIR)
            Material.LAVA_BUCKET
        }
        block.getProperty(States.WATERLOGGED) == "true" -> {
            instance.setBlock(position, block.withProperty(States.WATERLOGGED, "false"))
            Material.WATER_BUCKET
        }
        else -> return false
    }

    giveFluidBucket(player, hand, filled)
    return true
}

/**
 * Gives the player the filled bucket produced by a pickup. Creative players keep the empty bucket and
 * get the filled one added to their inventory if missing; survival players consume the empty bucket
 * and receive the filled one in hand or inventory.
 */
private fun giveFluidBucket(player: Player, hand: PlayerHand, material: Material) {
    val filled = ItemStack.of(material)
    if (player.gameMode == GameMode.CREATIVE) {
        if (player.inventory.getItemStacks().none { it.isSimilar(filled) }) {
            player.inventory.addItemStack(filled)
        }
        return
    }

    val remaining = player.getItemInHand(hand).consume(1)
    if (remaining.isAir) {
        player.setItemInHand(hand, filled)
    } else {
        player.setItemInHand(hand, remaining)
        if (!player.inventory.addItemStack(filled)) player.dropItem(filled)
    }
}
