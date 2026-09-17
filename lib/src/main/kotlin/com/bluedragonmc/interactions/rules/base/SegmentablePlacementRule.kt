package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementContext
import com.bluedragonmc.interactions.event.BlockPlacementEvent
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.Tags
import com.bluedragonmc.interactions.util.getBlockOrAir
import com.bluedragonmc.interactions.util.horizontalDirection
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace

/**
 * Base for blocks built from up to four segments (`pink_petals`, `wildflowers`, `leaf_litter`).
 *
 * These blocks face the player, merge into an existing block of the same type, and track their
 * segment count in a block-specific property ([amountProperty]).
 */
abstract class SegmentablePlacementRule(
    blocks: BlockSet,
    private val amountProperty: String,
) : StackingPlacementRule(blocks) {

    /** Whether this block may sit on top of [block]. */
    protected open fun mayPlaceOn(block: Block): Boolean = block.key() in Tags.SUPPORTS_VEGETATION

    override fun canMerge(existing: Block, context: PlacementContext, replacingClicked: Boolean): Boolean =
        !context.isSneaking && amount(existing) < MAX_AMOUNT

    override fun merge(clicked: Block, event: BlockPlacementEvent): Block =
        clicked.withProperty(amountProperty, (amount(clicked) + 1).coerceAtMost(MAX_AMOUNT).toString())

    override fun fresh(event: BlockPlacementEvent): Block = event.block
        .withProperty(States.FACING, event.player.position.horizontalDirection().oppositeFace.name.lowercase())
        .withProperty(amountProperty, "1")

    override fun canSurvive(state: Block, instance: Instance, position: BlockVec): Boolean =
        mayPlaceOn(instance.getBlockOrAir(position.relative(BlockFace.BOTTOM)))

    private fun amount(block: Block): Int = block.getProperty(amountProperty)?.toIntOrNull() ?: 1

    private companion object {
        const val MAX_AMOUNT = 4
    }
}
