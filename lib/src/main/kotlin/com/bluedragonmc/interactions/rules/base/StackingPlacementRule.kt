package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementContext
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.event.BlockPlacementEvent
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.Support
import com.bluedragonmc.interactions.util.Waterlogged
import com.bluedragonmc.interactions.util.getBlockOrAir
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Point
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace

/**
 * Base for blocks that merge into the clicked block: slabs, snow layers, candles, sea pickles and
 * turtle eggs.
 *
 * [getPlacementPosition] returns the clicked position when the clicked block is the same block and
 * [canMerge] accepts it, so the engine replaces the clicked block instead of placing a neighbor.
 */
abstract class StackingPlacementRule(blocks: BlockSet) : PlacementRule(blocks) {

    /** Whether [clicked] (which has the same key as the held block) should be merged into. */
    protected open fun canMerge(clicked: Block, context: PlacementContext): Boolean = true

    /** The merged state when placed into [clicked], an existing block of the same type. */
    protected abstract fun merge(clicked: Block, event: BlockPlacementEvent): Block

    /** The state when placed into a free/replaceable position. */
    protected abstract fun fresh(event: BlockPlacementEvent): Block

    /** Whether [state] can stay at [position]. */
    protected open fun canSurvive(state: Block, instance: Instance, position: BlockVec): Boolean = true

    init {
        onPlace {
            val target = instance.getBlockOrAir(blockPosition)
            val existing = when {
                target.key() == block.key() -> target
                clickedBlock.key() == block.key() -> clickedBlock
                else -> null
            }
            if (existing != null && !canMerge(existing, context)) {
                isCancelled = true
                return@onPlace
            }
            val result = if (existing != null) merge(existing, this) else fresh(this)
            if (!canSurvive(result, instance, blockPosition)) {
                isCancelled = true
                return@onPlace
            }
            block = result
        }

        onUpdate {
            if (!canSurvive(block, instance, blockPosition)) destroy()
        }
    }

    final override fun getPlacementPosition(context: PlacementContext): Point {
        if (context.clickedBlock.key() == context.block.key() && canMerge(context.clickedBlock, context)) {
            return context.clickedPosition
        }
        return context.placementPosition
    }
}

/** Two matching slabs merge into a double slab. */
open class SlabRule(blocks: BlockSet) : StackingPlacementRule(blocks) {

    override fun canMerge(clicked: Block, context: PlacementContext): Boolean {
        val type = clicked.getProperty(States.TYPE)
        if (type == "double") return false
        val above = context.cursorPosition.y() > 0.5
        val horizontal = context.blockFace.toDirection().horizontal()
        return if (type == "bottom") {
            context.blockFace == BlockFace.TOP || (above && horizontal)
        } else {
            context.blockFace == BlockFace.BOTTOM || (!above && horizontal)
        }
    }

    override fun merge(clicked: Block, event: BlockPlacementEvent): Block =
        clicked.withProperty(States.TYPE, "double").withProperty(States.WATERLOGGED, "false")

    override fun fresh(event: BlockPlacementEvent): Block {
        val clickedFace = event.blockFace
        val above = event.cursorPosition.y() > 0.5
        val bottom = clickedFace != BlockFace.BOTTOM && (clickedFace == BlockFace.TOP || !above)
        val state = event.block.withProperty(States.TYPE, if (bottom) "bottom" else "top")
        return Waterlogged.apply(state, event.instance, event.blockPosition)
    }
}

/** Snow layers stack up to eight, but only when clicked from above. */
open class SnowLayerRule(blocks: BlockSet) : StackingPlacementRule(blocks) {

    override fun canMerge(clicked: Block, context: PlacementContext): Boolean =
        layers(clicked) < MAX_LAYERS && context.blockFace == BlockFace.TOP

    override fun merge(clicked: Block, event: BlockPlacementEvent): Block =
        clicked.withProperty(States.LAYERS, (layers(clicked) + 1).coerceAtMost(MAX_LAYERS).toString())

    override fun fresh(event: BlockPlacementEvent): Block = event.block

    override fun canSurvive(state: Block, instance: Instance, position: BlockVec): Boolean {
        val below = instance.getBlockOrAir(position.relative(BlockFace.BOTTOM))
        if (below.key() == state.key() && layers(below) == MAX_LAYERS) return true
        return Support.isFaceFull(below, BlockFace.TOP)
    }

    private fun layers(block: Block): Int = block.getProperty(States.LAYERS)?.toIntOrNull() ?: 1

    private companion object {
        const val MAX_LAYERS = 8
    }
}

/** Up to four candles per block. */
open class CandleRule(blocks: BlockSet) : StackingPlacementRule(blocks) {

    override fun canMerge(clicked: Block, context: PlacementContext): Boolean =
        !context.isSneaking && candles(clicked) < MAX_CANDLES

    override fun merge(clicked: Block, event: BlockPlacementEvent): Block =
        clicked.withProperty(States.CANDLES, (candles(clicked) + 1).coerceAtMost(MAX_CANDLES).toString())

    override fun fresh(event: BlockPlacementEvent): Block {
        val state = event.block.withProperty(States.CANDLES, "1").withProperty(States.LIT, "false")
        return Waterlogged.apply(state, event.instance, event.blockPosition)
    }

    override fun canSurvive(state: Block, instance: Instance, position: BlockVec): Boolean =
        Support.canSupportCenter(instance, position.relative(BlockFace.BOTTOM), BlockFace.TOP)

    private fun candles(block: Block): Int = block.getProperty(States.CANDLES)?.toIntOrNull() ?: 1

    private companion object {
        const val MAX_CANDLES = 4
    }
}

/** Up to four pickles per block, waterlogged when placed in water. */
open class SeaPickleRule(blocks: BlockSet) : StackingPlacementRule(blocks) {

    override fun canMerge(clicked: Block, context: PlacementContext): Boolean =
        !context.isSneaking && pickles(clicked) < MAX_PICKLES

    override fun merge(clicked: Block, event: BlockPlacementEvent): Block =
        clicked.withProperty(States.PICKLES, (pickles(clicked) + 1).coerceAtMost(MAX_PICKLES).toString())

    override fun fresh(event: BlockPlacementEvent): Block {
        val state = event.block.withProperty(States.PICKLES, "1")
        return Waterlogged.apply(state, event.instance, event.blockPosition)
    }

    override fun canSurvive(state: Block, instance: Instance, position: BlockVec): Boolean =
        Support.isFaceFull(instance, position.relative(BlockFace.BOTTOM), BlockFace.TOP)

    private fun pickles(block: Block): Int = block.getProperty(States.PICKLES)?.toIntOrNull() ?: 1

    private companion object {
        const val MAX_PICKLES = 4
    }
}

/** Up to four eggs per block. */
open class TurtleEggRule(blocks: BlockSet) : StackingPlacementRule(blocks) {

    override fun canMerge(clicked: Block, context: PlacementContext): Boolean =
        !context.isSneaking && eggs(clicked) < MAX_EGGS

    override fun merge(clicked: Block, event: BlockPlacementEvent): Block =
        clicked.withProperty(States.EGGS, (eggs(clicked) + 1).coerceAtMost(MAX_EGGS).toString())

    override fun fresh(event: BlockPlacementEvent): Block = event.block

    private fun eggs(block: Block): Int = block.getProperty(States.EGGS)?.toIntOrNull() ?: 1

    private companion object {
        const val MAX_EGGS = 4
    }
}
