package com.bluedragonmc.interactions.rules.interaction.misc

import com.bluedragonmc.interactions.BlockInteractionRule
import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.blockCenter
import com.bluedragonmc.interactions.util.environmentValue
import com.bluedragonmc.interactions.util.heldItem
import com.bluedragonmc.interactions.util.isSecondaryUseActive
import com.bluedragonmc.interactions.util.playBlockSound
import com.bluedragonmc.interactions.util.setHeldItem
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.instance.block.Block
import net.minestom.server.item.Material
import net.minestom.server.item.component.SwingAnimation
import net.minestom.server.sound.SoundEvent
import net.minestom.server.world.attribute.EnvironmentAttribute

/**
 * Respawn anchors are charged with glowstone and set the player's respawn point when used with an
 * empty hand, exploding in dimensions where they do not work.
 */
class RespawnAnchorInteractionRule : BlockInteractionRule(BlockSet.from(Block.RESPAWN_ANCHOR)) {
    init {
        onInteract {
            if (isSecondaryUseActive()) return@onInteract
            val charges = block.getProperty(States.CHARGES)?.toIntOrNull() ?: 0

            if (heldItem.material() == Material.GLOWSTONE && charges < 4) {
                isBlockingItemUse = true
                instance.setBlock(blockPosition, block.withProperty(States.CHARGES, (charges + 1).toString()))
                instance.playBlockSound(SoundEvent.BLOCK_RESPAWN_ANCHOR_CHARGE, blockPosition.blockCenter())
                if (player.gameMode != GameMode.CREATIVE) setHeldItem(heldItem.consume(1))
                return@onInteract
            }

            if (charges == 0) return@onInteract
            isBlockingItemUse = true

            if (!environmentValue(instance, EnvironmentAttribute.RESPAWN_ANCHOR_WORKS, false)) {
                player.swingHand(hand, SwingAnimation.DEFAULT)
                instance.setBlock(blockPosition, Block.AIR)
                instance.explode(
                    (blockPosition.x() + 0.5).toFloat(),
                    (blockPosition.y() + 0.5).toFloat(),
                    (blockPosition.z() + 0.5).toFloat(),
                    5.0f,
                )
                return@onInteract
            }

            val respawn = Pos(blockPosition.x() + 0.5, blockPosition.y() + 0.1, blockPosition.z() + 0.5)
            if (!player.respawnPoint.samePoint(respawn)) {
                player.swingHand(hand, SwingAnimation.DEFAULT)
                player.respawnPoint = respawn
                instance.playBlockSound(SoundEvent.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, blockPosition.blockCenter())
            }
        }
    }
}
