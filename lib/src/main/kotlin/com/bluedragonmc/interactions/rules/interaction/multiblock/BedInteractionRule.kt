package com.bluedragonmc.interactions.rules.interaction.multiblock

import com.bluedragonmc.interactions.BlockInteractionRule
import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.plus
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.blockFace
import com.bluedragonmc.interactions.util.directionOf
import com.bluedragonmc.interactions.util.environmentValue
import com.bluedragonmc.interactions.util.isSecondaryUseActive
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.component.SwingAnimation
import net.minestom.server.utils.Direction
import net.minestom.server.world.attribute.BedRule
import net.minestom.server.world.attribute.EnvironmentAttribute

/**
 * Sleeping in a bed sets the player's respawn point (when the dimension's bed rule allows it), or
 * destroys and explodes the bed where sleeping is forbidden.
 */
class BedInteractionRule : BlockInteractionRule(BlockSet.fromTag("minecraft:beds") + BlockSet.from(Block.STRAW_BED)) {
    init {
        onInteract {
            if (isSecondaryUseActive()) return@onInteract
            isBlockingItemUse = true
            player.swingHand(hand, SwingAnimation.DEFAULT)

            var bedPosition = blockPosition
            if (block.getProperty(States.PART) != "head") {
                val facing = block.getProperty(States.FACING)?.let(::directionOf) ?: Direction.NORTH
                bedPosition = blockPosition.relative(facing.blockFace)
            }

            val bedRule = if (block.key() == Block.STRAW_BED.key()) {
                environmentValue(instance, EnvironmentAttribute.STRAW_BED_RULE, BedRule.DESTROY_ON_LEAVE)
            } else {
                environmentValue(instance, EnvironmentAttribute.BED_RULE, BedRule.CAN_SLEEP_WHEN_DARK)
            }
            if (bedRule.destroyOnUse) {
                instance.setBlock(bedPosition, Block.AIR)
                instance.explode(
                    (bedPosition.x() + 0.5).toFloat(),
                    (bedPosition.y() + 0.5).toFloat(),
                    (bedPosition.z() + 0.5).toFloat(),
                    5.0f,
                )
                return@onInteract
            }

            val canSetSpawn = bedRule.canSetSpawn == BedRule.Rule.ALWAYS ||
                (bedRule.canSetSpawn == BedRule.Rule.WHEN_DARK && isDark(instance))
            if (canSetSpawn) {
                player.respawnPoint = Pos(bedPosition.x() + 0.5, bedPosition.y() + 0.1, bedPosition.z() + 0.5)
            }
        }
    }

    private fun isDark(instance: Instance): Boolean {
        val time = instance.time % 24000L
        return time in 12500L until 23500L
    }
}
