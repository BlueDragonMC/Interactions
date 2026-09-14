package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.util.Tags
import com.bluedragonmc.interactions.util.Waterlogged
import com.bluedragonmc.interactions.util.getBlockOrAir
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace

/**
 * Base for ground plants that only need a supporting block below: crops, saplings, flowers, mushrooms
 * and similar vegetation.
 */
abstract class BushPlacementRule(blocks: BlockSet) : PlacementRule(blocks) {

    /** Whether this plant may sit on top of [block]. */
    protected open fun mayPlaceOn(block: Block): Boolean = block.key() in Tags.SUPPORTS_VEGETATION

    /** Whether [state] can stay at [position]. */
    protected open fun canSurvive(state: Block, instance: Instance, position: BlockVec): Boolean =
        mayPlaceOn(instance.getBlockOrAir(position.relative(BlockFace.BOTTOM)))

    init {
        onPlace {
            if (!canSurvive(block, instance, blockPosition)) {
                isCancelled = true
                return@onPlace
            }
            block = Waterlogged.apply(block, instance, blockPosition)
        }

        onUpdate {
            if (fromFace == BlockFace.BOTTOM && !canSurvive(block, instance, blockPosition)) destroy()
        }
    }
}
