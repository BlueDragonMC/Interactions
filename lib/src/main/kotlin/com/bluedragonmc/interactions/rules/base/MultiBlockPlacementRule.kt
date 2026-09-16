package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.event.BlockPlacementEvent
import com.bluedragonmc.interactions.util.Waterlogged
import com.bluedragonmc.interactions.util.getBlockOrAir
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block

/**
 * Base for multi-block structures: doors, beds, tall flowers/grass, pitcher crops and dripleaf.
 * Breaking either half removes the other.
 */
abstract class MultiBlockPlacementRule(blocks: BlockSet) : PlacementRule(blocks) {

    /** The state placed at the primary position. */
    protected abstract fun primaryState(event: BlockPlacementEvent): Block

    /** The position of the secondary block, relative to the primary placement position. */
    protected abstract fun secondaryPosition(primary: Block, event: BlockPlacementEvent): BlockVec

    /** The state of the secondary block, given the (waterlogged) primary state. */
    protected abstract fun secondaryState(primary: Block, event: BlockPlacementEvent): Block

    /** The position of the other half when [broken] is destroyed, or null if not applicable. */
    protected abstract fun pairPosition(broken: Block, position: BlockVec): BlockVec?

    /** Whether the secondary position is free. */
    protected open fun canPlaceSecondary(instance: Instance, position: BlockVec): Boolean =
        instance.getBlockOrAir(position).air()

    init {
        onPlace {
            val primary = primaryState(this)
            val secondaryPosition = secondaryPosition(primary, this)
            if (!canPlaceSecondary(instance, secondaryPosition)) {
                isCancelled = true
                return@onPlace
            }
            block = Waterlogged.apply(primary, instance, blockPosition)
            placeAt(
                secondaryPosition,
                Waterlogged.apply(secondaryState(block, this), instance, secondaryPosition),
            )
        }

        onDestroy {
            if (isCancelled) return@onDestroy
            val pair = pairPosition(block, blockPosition) ?: return@onDestroy
            if (instance.getBlockOrAir(pair).key() in keys) instance.setBlock(pair, Block.AIR)
        }
    }
}
