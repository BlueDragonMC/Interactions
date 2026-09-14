package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.event.BlockPlacementEvent
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.Waterlogged
import com.bluedragonmc.interactions.util.axisName
import com.bluedragonmc.interactions.util.horizontalDirection
import com.bluedragonmc.interactions.util.nearestLookingDirection
import com.bluedragonmc.interactions.util.nearestLookingVerticalDirection
import net.minestom.server.instance.block.Block

/**
 * Base for blocks whose placement state depends only on the player's look and the clicked face.
 *
 * Used when the block has a `facing` or `axis` property that depends on where the player is looking.
 */
abstract class DirectionalPlacementRule(blocks: BlockSet) : PlacementRule(blocks) {

    init {
        onPlace {
            block = Waterlogged.apply(placementState(this), instance, blockPosition)
        }
    }

    /** Computes the block state to place for [event]. */
    protected abstract fun placementState(event: BlockPlacementEvent): Block
}

/**
 * Horizontal-facing blocks. [facesPlayer] selects whether the block faces the player
 * (furnaces, pumpkins, glazed terracotta) or away from them (ladders, wall signs).
 */
open class HorizontalFacingRule(
    blocks: BlockSet,
    private val facesPlayer: Boolean = true,
) : DirectionalPlacementRule(blocks) {

    override fun placementState(event: BlockPlacementEvent): Block {
        val look = event.player.position.horizontalDirection()
        val facing = if (facesPlayer) look.oppositeFace else look
        return event.block.withProperty(States.FACING, facing.name.lowercase())
    }
}

/** The `axis` comes from the clicked face. */
open class AllAxisRule(blocks: BlockSet) : DirectionalPlacementRule(blocks) {

    override fun placementState(event: BlockPlacementEvent): Block =
        event.block.withProperty(States.AXIS, event.blockFace.axisName)
}

/** Blocks whose `facing` equals the clicked face (e.g. amethyst clusters, end rods). */
open class ClickFacingRule(blocks: BlockSet) : DirectionalPlacementRule(blocks) {

    override fun placementState(event: BlockPlacementEvent): Block =
        event.block.withProperty(States.FACING, event.blockFace.toDirection().name.lowercase())
}

/** Blocks whose `facing` follows the nearest vertical look direction (`up`/`down`). */
open class VerticalFacingRule(blocks: BlockSet) : DirectionalPlacementRule(blocks) {

    override fun placementState(event: BlockPlacementEvent): Block {
        val facing = event.player.position.nearestLookingVerticalDirection()
        return event.block.withProperty(States.FACING, facing.name.lowercase())
    }
}

/** Blocks whose `facing` follows the nearest looking direction of any axis. */
open class NearestFacingRule(
    blocks: BlockSet,
    private val facesPlayer: Boolean = true,
) : DirectionalPlacementRule(blocks) {

    override fun placementState(event: BlockPlacementEvent): Block {
        val nearest = event.player.position.nearestLookingDirection()
        val facing = if (facesPlayer) nearest.opposite() else nearest
        return event.block.withProperty(States.FACING, facing.name.lowercase())
    }
}
