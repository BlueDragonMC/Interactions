package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.util.*
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.utils.Direction

/**
 * Base for walls, whose block state is a `none`/`low`/`tall` connection per horizontal side plus a
 * boolean `up` for whether the center post reaches the top of the block.
 *
 * A side connects when the neighbor is a wall, has a sturdy face toward this block, is bars/panes,
 * or is a fence gate. A connected side is `tall` when the block above covers the footprint that
 * side's arm occupies, and `low` otherwise.
 *
 * The post stays flush when a straight, raised run passes through the block, i.e. two opposite
 * sides are both `tall`. Otherwise, it is raised when the block above is a wall with a raised post;
 * when the sides are empty or their presence is unbalanced (north/south differ, or east/west
 * differ); when the block above is a post override (torch, sign, pressure plate, ...); or when the
 * block above covers the post's center.
 */
abstract class WallPlacementRule(blocks: BlockSet) : PlacementRule(blocks) {

    init {
        onPlace {
            block = compute(block, instance, blockPosition)
        }

        onUpdate {
            resultBlock = compute(resultBlock, instance, blockPosition)
        }
    }

    /** Whether this wall connects toward [neighbor] (with [faceSolid] its face toward the wall). */
    protected open fun canConnect(neighbor: Block, faceSolid: Boolean, direction: Direction): Boolean =
        neighbor.key() in keys ||
            (!Support.isExceptionForConnection(neighbor) && faceSolid) ||
            Connections.isIronBarsOrPane(neighbor) ||
            Connections.isFenceGate(neighbor)

    private fun compute(state: Block, instance: Instance, position: BlockVec): Block {
        var result = state
        val above = instance.getBlockOrAir(position.relative(BlockFace.TOP))
        val aboveRects = Support.faceRectangles(above, BlockFace.BOTTOM)

        for (face in HORIZONTAL) {
            val neighborPosition = position.relative(face)
            val neighbor = instance.getBlockOrAir(neighborPosition)
            val faceSolid = Support.isFaceSturdy(instance, neighborPosition, face.oppositeFace)
            val connects = canConnect(neighbor, faceSolid, face.oppositeFace.toDirection())
            val side = when {
                !connects -> "none"
                Support.covers(aboveRects, ARM_RECTS.getValue(face)) -> "tall"
                else -> "low"
            }
            result = result.withProperty(States.connection(face), side)
        }

        result = result.withProperty(States.UP, shouldRaisePost(result, above, aboveRects).toString())
        return Waterlogged.apply(result, instance, position)
    }

    /** Whether the center post should extend upward, given the already-computed sides. */
    private fun shouldRaisePost(state: Block, above: Block, aboveRects: List<Rect>): Boolean {
        if (above.key() in keys && above.getProperty(States.UP) == "true") return true

        val northNone = state.getProperty(States.NORTH) == "none"
        val southNone = state.getProperty(States.SOUTH) == "none"
        val eastNone = state.getProperty(States.EAST) == "none"
        val westNone = state.getProperty(States.WEST) == "none"

        val hasCorner = (northNone && southNone && westNone && eastNone) ||
            northNone != southNone ||
            westNone != eastNone
        if (hasCorner) return true

        val hasHighWall = (state.getProperty(States.NORTH) == "tall" && state.getProperty(States.SOUTH) == "tall") ||
            (state.getProperty(States.EAST) == "tall" && state.getProperty(States.WEST) == "tall")
        if (hasHighWall) return false

        return above.key() in Tags.WALL_POST_OVERRIDE || Support.covers(aboveRects, POST_RECT)
    }

    private companion object {
        val HORIZONTAL = listOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)

        // The footprint each side's arm occupies, projected onto the block above's bottom face.
        // `u` is x and `v` is z. An arm is 2/16 wide, centered on the perpendicular axis, and
        // reaches 9/16 out from the center toward that side.
        val ARM_RECTS: Map<BlockFace, Rect> = mapOf(
            BlockFace.NORTH to Rect(7.0 / 16.0, 0.0, 9.0 / 16.0, 9.0 / 16.0),
            BlockFace.EAST to Rect(7.0 / 16.0, 7.0 / 16.0, 1.0, 9.0 / 16.0),
            BlockFace.SOUTH to Rect(7.0 / 16.0, 7.0 / 16.0, 9.0 / 16.0, 1.0),
            BlockFace.WEST to Rect(0.0, 7.0 / 16.0, 9.0 / 16.0, 9.0 / 16.0),
        )

        // The center 2/16 x 2/16 column a raised post occupies, projected onto the same face.
        val POST_RECT = Rect(7.0 / 16.0, 7.0 / 16.0, 9.0 / 16.0, 9.0 / 16.0)
    }
}
