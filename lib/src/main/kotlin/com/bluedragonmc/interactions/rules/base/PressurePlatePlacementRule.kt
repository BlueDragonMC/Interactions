package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.util.Support
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace

/**
 * Base for pressure plates: placed unpowered on a supporting block below, removed when that support
 * disappears.
 */
abstract class PressurePlatePlacementRule(blocks: BlockSet) : PlacementRule(blocks) {

    init {
        onPlace {
            if (!canSurvive(block, instance, blockPosition)) isCancelled = true
        }

        onUpdate {
            if (fromFace == BlockFace.BOTTOM && !canSurvive(block, instance, blockPosition)) destroy()
        }
    }

    /** Whether [state] can stay at [position]. */
    protected open fun canSurvive(state: Block, instance: Instance, position: BlockVec): Boolean {
        val below = position.relative(BlockFace.BOTTOM)
        return Support.canSupportRigidBlock(instance, below) || Support.canSupportCenter(instance, below, BlockFace.TOP)
    }
}
