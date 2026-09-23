package com.bluedragonmc.interactions.rules.interaction.misc

import com.bluedragonmc.interactions.BlockInteractionRule
import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.blockCenter
import com.bluedragonmc.interactions.util.consumeHeldItem
import com.bluedragonmc.interactions.util.damageHeldItem
import com.bluedragonmc.interactions.util.heldMaterial
import com.bluedragonmc.interactions.util.isSecondaryUseActive
import com.bluedragonmc.interactions.util.playBlockSoundExcept
import net.minestom.server.entity.GameMode
import net.minestom.server.item.Material
import net.minestom.server.sound.SoundEvent
import java.util.concurrent.ThreadLocalRandom

/**
 * Candles can be lit with flint and steel or a fire charge and extinguished with an empty hand.
 */
class CandleInteractionRule : BlockInteractionRule(BlockSet.fromTag("minecraft:candles")) {
    init {
        onInteract {
            val lit = block.getProperty(States.LIT)

            if (!isSecondaryUseActive() && lit != "true" && block.getProperty(States.WATERLOGGED) != "true" &&
                (heldMaterial == Material.FLINT_AND_STEEL || heldMaterial == Material.FIRE_CHARGE)
            ) {
                isBlockingItemUse = true
                instance.setBlock(blockPosition, block.withProperty(States.LIT, "true"))

                val fireCharge = heldMaterial == Material.FIRE_CHARGE
                val random = ThreadLocalRandom.current()
                val pitch = if (fireCharge) {
                    (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f
                } else {
                    random.nextFloat() * 0.4f + 0.8f
                }
                instance.playBlockSoundExcept(
                    player,
                    if (fireCharge) SoundEvent.ITEM_FIRECHARGE_USE else SoundEvent.ITEM_FLINTANDSTEEL_USE,
                    blockPosition.blockCenter(),
                    pitch = pitch,
                )

                if (fireCharge) consumeHeldItem(1) else damageHeldItem(1)
                return@onInteract
            }

            if (isSecondaryUseActive()) return@onInteract
            if (lit != "true" || player.gameMode == GameMode.ADVENTURE || player.gameMode == GameMode.SPECTATOR) return@onInteract

            isBlockingItemUse = true
            instance.setBlock(blockPosition, block.withProperty(States.LIT, "false"))
            instance.playBlockSoundExcept(
                player,
                SoundEvent.BLOCK_CANDLE_EXTINGUISH,
                blockPosition.blockCenter(),
                pitch = ThreadLocalRandom.current().nextFloat() * 0.4f + 0.8f,
            )
        }
    }
}
