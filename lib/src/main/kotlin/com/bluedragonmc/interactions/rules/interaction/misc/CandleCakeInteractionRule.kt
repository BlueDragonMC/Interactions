package com.bluedragonmc.interactions.rules.interaction.misc

import com.bluedragonmc.interactions.BlockInteractionRule
import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.blockCenter
import com.bluedragonmc.interactions.util.consumeHeldItem
import com.bluedragonmc.interactions.util.damageHeldItem
import com.bluedragonmc.interactions.util.dropItemStack
import com.bluedragonmc.interactions.util.heldItem
import com.bluedragonmc.interactions.util.heldMaterial
import com.bluedragonmc.interactions.util.isSecondaryUseActive
import com.bluedragonmc.interactions.util.playBlockSoundExcept
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.sound.SoundEvent
import java.util.concurrent.ThreadLocalRandom

/**
 * Candle cakes can be lit and extinguished, and eating a slice ejects the candle.
 */
class CandleCakeInteractionRule : BlockInteractionRule(BlockSet.matching { it.endsWith("candle_cake") }) {
    init {
        onInteract {
            val lit = block.getProperty(States.LIT) == "true"

            if (heldMaterial == Material.FLINT_AND_STEEL || heldMaterial == Material.FIRE_CHARGE) {
                if (!lit) light(this) else extinguish(this)
                return@onInteract
            }

            if (heldItem.isAir && cursorPosition.y() > 0.5 && lit) {
                extinguish(this)
                return@onInteract
            }

            if (isSecondaryUseActive()) return@onInteract
            if (!canEat(player)) return@onInteract
            isBlockingItemUse = true

            val candle = candleFor(block)
            eatCake(instance, player, blockPosition, Block.CAKE, bites = 0)
            if (candle != null) instance.dropItemStack(blockPosition, ItemStack.of(candle))
        }
    }

    private fun light(event: PlayerBlockInteractEvent) {
        event.isBlockingItemUse = true
        event.instance.setBlock(event.blockPosition, event.block.withProperty(States.LIT, "true"))

        val fireCharge = event.heldMaterial == Material.FIRE_CHARGE
        val random = ThreadLocalRandom.current()
        val pitch = if (fireCharge) {
            (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f
        } else {
            random.nextFloat() * 0.4f + 0.8f
        }
        event.instance.playBlockSoundExcept(
            event.player,
            if (fireCharge) SoundEvent.ITEM_FIRECHARGE_USE else SoundEvent.ITEM_FLINTANDSTEEL_USE,
            event.blockPosition.blockCenter(),
            pitch = pitch,
        )

        if (fireCharge) event.consumeHeldItem(1) else event.damageHeldItem(1)
    }

    private fun extinguish(event: PlayerBlockInteractEvent) {
        val gameMode = event.player.gameMode
        if (gameMode == GameMode.ADVENTURE || gameMode == GameMode.SPECTATOR) return
        event.isBlockingItemUse = true
        event.instance.setBlock(event.blockPosition, event.block.withProperty(States.LIT, "false"))
        event.instance.playBlockSoundExcept(
            event.player,
            SoundEvent.BLOCK_CANDLE_EXTINGUISH,
            event.blockPosition.blockCenter(),
            pitch = ThreadLocalRandom.current().nextFloat() * 0.4f + 0.8f,
        )
    }
}
