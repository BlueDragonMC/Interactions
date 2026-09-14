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
 * Base for walls, whose connections are `WallSide` values (`none`/`low`/`tall`) plus an `up` post.
 *
 * The `low`/`tall` distinction is approximated from whether the block above is a raised wall or a
 * full block; the post is raised at corners and under raised walls.
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

    override fun maxUpdateDistance(): Int = 1

    /** Whether this wall connects toward [neighbor] (with [faceSolid] its face toward the wall). */
    protected open fun canConnect(neighbor: Block, faceSolid: Boolean, direction: Direction): Boolean =
        neighbor.key() in keys ||
            (!Support.isExceptionForConnection(neighbor) && faceSolid) ||
            Connections.isIronBarsOrPane(neighbor) ||
            Connections.isFenceGate(neighbor)

    private fun compute(state: Block, instance: Instance, position: BlockVec): Block {
        var result = state
        val above = instance.getBlockOrAir(position.relative(BlockFace.TOP))
        val aboveRaisedWall = above.key() in keys && above.getProperty(States.UP) == "true"
        val tall = aboveRaisedWall || Support.isFaceFull(above, BlockFace.BOTTOM)

        var connectedCount = 0
        for (face in HORIZONTAL) {
            val neighborPosition = position.relative(face)
            val neighbor = instance.getBlockOrAir(neighborPosition)
            val faceSolid = Support.isFaceSturdy(instance, neighborPosition, face.oppositeFace)
            val connects = canConnect(neighbor, faceSolid, face.oppositeFace.toDirection())
            if (connects) connectedCount++
            result = result.withProperty(
                States.connection(face),
                if (!connects) "none" else if (tall) "tall" else "low",
            )
        }

        val north = result.getProperty(States.NORTH) != "none"
        val south = result.getProperty(States.SOUTH) != "none"
        val east = result.getProperty(States.EAST) != "none"
        val west = result.getProperty(States.WEST) != "none"
        val corner = connectedCount == 0 || north != south || east != west
        result = result.withProperty(States.UP, (aboveRaisedWall || corner).toString())

        return Waterlogged.apply(result, instance, position)
    }

    private companion object {
        val HORIZONTAL = listOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)
    }
}
