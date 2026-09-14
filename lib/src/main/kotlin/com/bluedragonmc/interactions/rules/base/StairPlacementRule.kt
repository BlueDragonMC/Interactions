package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.event.BlockPlacementEvent
import com.bluedragonmc.interactions.util.Fluids
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.axisName
import com.bluedragonmc.interactions.util.blockFace
import com.bluedragonmc.interactions.util.counterClockWise
import com.bluedragonmc.interactions.util.directionOf
import com.bluedragonmc.interactions.util.getBlockOrAir
import com.bluedragonmc.interactions.util.horizontalDirection
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.utils.Direction

/**
 * Placement derives `facing` from the player's yaw and `half` from the clicked face/cursor, then
 * computes `shape` (straight/inner/outer) from the surrounding stairs. Updates recompute `shape`
 * when a horizontal neighbor changes.
 */
open class StairPlacementRule(blocks: BlockSet) : PlacementRule(blocks) {

    init {
        onPlace {
            val state = block
                .withProperty(States.FACING, player.position.horizontalDirection().name.lowercase())
                .withProperty(States.HALF, half(this))
                .withProperty(
                    States.WATERLOGGED,
                    Fluids.isWater(instance, blockPosition).toString(),
                )
            block = state.withProperty(States.SHAPE, stairsShape(state, instance, blockPosition))
        }

        onUpdate {
            if (fromFace.toDirection().vertical()) return@onUpdate
            resultBlock = resultBlock.withProperty(States.SHAPE, stairsShape(resultBlock, instance, blockPosition))
        }
    }

    private fun half(event: BlockPlacementEvent): String {
        val clickedFace = event.blockFace
        val above = event.cursorPosition.y() > 0.5
        val bottom = clickedFace != BlockFace.BOTTOM && (clickedFace == BlockFace.TOP || !above)
        return if (bottom) "bottom" else "top"
    }

    private fun stairsShape(state: Block, instance: Instance, position: BlockVec): String {
        val facing = directionOf(state.getProperty(States.FACING)!!)
        val half = state.getProperty(States.HALF)

        val behind = instance.getBlockOrAir(position.relative(facing.blockFace))
        if (behind.key() in keys && behind.getProperty(States.HALF) == half) {
            val behindFacing = directionOf(behind.getProperty(States.FACING)!!)
            if (behindFacing.axisName != facing.axisName && canTakeShape(state, instance, position, behindFacing.opposite())) {
                return if (behindFacing == facing.counterClockWise()) "outer_left" else "outer_right"
            }
        }

        val front = instance.getBlockOrAir(position.relative(facing.opposite().blockFace))
        if (front.key() in keys && front.getProperty(States.HALF) == half) {
            val frontFacing = directionOf(front.getProperty(States.FACING)!!)
            if (frontFacing.axisName != facing.axisName && canTakeShape(state, instance, position, frontFacing)) {
                return if (frontFacing == facing.counterClockWise()) "inner_left" else "inner_right"
            }
        }

        return "straight"
    }

    private fun canTakeShape(state: Block, instance: Instance, position: BlockVec, neighbor: Direction): Boolean {
        val neighborState = instance.getBlockOrAir(position.relative(neighbor.blockFace))
        if (neighborState.key() !in keys) return true
        return neighborState.getProperty(States.FACING) != state.getProperty(States.FACING) ||
            neighborState.getProperty(States.HALF) != state.getProperty(States.HALF)
    }
}
