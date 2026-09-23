package com.bluedragonmc.interactions.rules.interaction.misc

import com.bluedragonmc.interactions.BlockInteractionRule
import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.util.*
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.Material
import net.minestom.server.sound.SoundEvent

/** Eating cake removes slices and advances the `bites` property; a candle turns it into candle cake. */
class CakeInteractionRule : BlockInteractionRule(BlockSet.from(Block.CAKE)) {
    init {
        onInteract {
            if (isSecondaryUseActive()) return@onInteract
            isBlockingItemUse = true

            val bites = block.getProperty(States.BITES)?.toIntOrNull() ?: 0
            val candleCake = candleCakeFor(heldMaterial)
            if (candleCake != null && bites == 0) {
                instance.setBlock(blockPosition, candleCake)
                instance.playBlockSound(SoundEvent.BLOCK_CAKE_ADD_CANDLE, blockPosition.blockCenter())
                consumeHeldItem(1)
                return@onInteract
            }

            if (!canEat(player)) return@onInteract
            eatCake(instance, player, blockPosition, block, bites)
        }
    }
}

/** Creative players can always eat, others only when hungry. */
internal fun canEat(player: Player): Boolean =
    player.gameMode == GameMode.CREATIVE || player.food < 20

/** The candle-cake block produced by placing [material] (a candle) on an unbitten cake. */
internal fun candleCakeFor(material: Material): Block? {
    val path = material.key().value()
    if (!path.endsWith("candle")) return null
    return Block.fromKey("minecraft:${path}_cake")
}

/** The candle material removed when a candle cake is eaten. */
internal fun candleFor(candleCake: Block): Material? =
    Material.fromKey("minecraft:${candleCake.key().value().removeSuffix("_cake")}")

internal fun eatCake(instance: Instance, player: Player, position: BlockVec, block: Block, bites: Int) {
    val newFood = (player.food + 2).coerceAtMost(20)
    player.food = newFood
    player.foodSaturation = minOf(newFood.toFloat(), player.foodSaturation + 0.4f)
    if (bites >= 6) {
        instance.setBlock(position, Block.AIR)
    } else {
        instance.setBlock(position, block.withProperty(States.BITES, (bites + 1).toString()))
    }
}
