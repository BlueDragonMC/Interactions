package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.Support
import com.bluedragonmc.interactions.util.Waterlogged
import com.bluedragonmc.interactions.util.horizontalDirection
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace

/**
 * Base for rails: `shape` follows the player's horizontal look (east/west or north/south) and the
 * rail is removed when its supporting block below disappears.
 */
abstract class RailPlacementRule(blocks: BlockSet) : PlacementRule(blocks) {

    init {
        onPlace {
            val look = player.position.horizontalDirection()
            val eastWest = look == BlockFace.EAST || look == BlockFace.WEST
            val state = block.withProperty(States.SHAPE, if (eastWest) "east_west" else "north_south")
            if (!canSurvive(state, instance, blockPosition)) {
                isCancelled = true
                return@onPlace
            }
            block = Waterlogged.apply(state, instance, blockPosition)
        }

        onUpdate {
            if (fromFace == BlockFace.BOTTOM && !canSurvive(block, instance, blockPosition)) destroy()
        }
    }

    /** Whether [state] can stay at [position]. */
    protected open fun canSurvive(state: Block, instance: Instance, position: BlockVec): Boolean =
        Support.canSupportRigidBlock(instance, position.relative(BlockFace.BOTTOM))
}
