package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.event.BlockPlacementEvent
import com.bluedragonmc.interactions.util.*
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.utils.Direction

/**
 * Base for blocks mounted on a face of a support block: torches, signs, banners, skulls, levers,
 * lanterns, amethyst clusters, chains, and bells.
 *
 * Placement tries each direction ordered by the player's look and stops at the first viable one.
 * Updates destroy the block when its support disappears.
 *
 * Subclasses decide how a direction maps to a block state ([stateForDirection]) and which support
 * shape is required ([canSurvive]).
 */
abstract class WallMountedPlacementRule(blocks: BlockSet) : PlacementRule(blocks) {

    init {
        onPlace {
            isCancelled = true
            for (direction in directions(this)) {
                val candidate = stateForDirection(direction, this) ?: continue
                if (!canSurvive(candidate, instance, blockPosition)) continue
                block = Waterlogged.apply(candidate, instance, blockPosition)
                isCancelled = false
                return@onPlace
            }
        }

        onUpdate {
            if (!canSurvive(block, instance, blockPosition)) destroy()
        }
    }

    /** Candidate directions in priority order. */
    protected open fun directions(event: BlockPlacementEvent): List<Direction> =
        event.player.position.orderedDirections(event.blockFace.oppositeFace.toDirection())

    /** The state to place when mounted in [direction], or null if this direction is not applicable. */
    protected abstract fun stateForDirection(direction: Direction, event: BlockPlacementEvent): Block?

    /** Whether [state] can stay mounted at [position]. */
    protected abstract fun canSurvive(state: Block, instance: Instance, position: BlockVec): Boolean
}

/**
 * A `face` of `wall`/`floor`/`ceiling` plus a horizontal `facing` (buttons, levers).
 */
abstract class FaceAttachedPlacementRule(blocks: BlockSet) : WallMountedPlacementRule(blocks) {

    override fun stateForDirection(direction: Direction, event: BlockPlacementEvent): Block? {
        val face: String
        val facing: BlockFace
        if (direction.vertical()) {
            face = if (direction == Direction.UP) "ceiling" else "floor"
            facing = event.player.position.horizontalDirection()
        } else {
            face = "wall"
            facing = direction.opposite().blockFace
        }
        return event.block
            .withProperty(States.FACE, face)
            .withProperty(States.FACING, facing.name.lowercase())
    }

    override fun canSurvive(state: Block, instance: Instance, position: BlockVec): Boolean {
        val connected = connectedDirection(state)
        val support = position.relative(connected.opposite().blockFace)
        return Support.isFaceSturdy(instance, support, connected.blockFace)
    }

    private fun connectedDirection(state: Block): Direction = when (state.getProperty(States.FACE)) {
        "ceiling" -> Direction.DOWN
        "floor" -> Direction.UP
        else -> directionOf(state.getProperty(States.FACING)!!)
    }
}

/** A wall torch only mounts on a horizontal face and stores `facing`. */
abstract class WallTorchPlacementRule(blocks: BlockSet) : WallMountedPlacementRule(blocks) {

    override fun stateForDirection(direction: Direction, event: BlockPlacementEvent): Block? {
        if (direction.vertical()) return null
        val facing = direction.opposite()
        return event.block.withProperty(States.FACING, facing.name.lowercase())
    }

    override fun canSurvive(state: Block, instance: Instance, position: BlockVec): Boolean {
        val facing = directionOf(state.getProperty(States.FACING)!!)
        val support = position.relative(facing.opposite().blockFace)
        return Support.isFaceSturdy(instance, support, facing.blockFace)
    }
}

/** A `hanging` boolean derived from the supporting face. */
abstract class HangingPlacementRule(blocks: BlockSet) : WallMountedPlacementRule(blocks) {

    override fun stateForDirection(direction: Direction, event: BlockPlacementEvent): Block? {
        if (direction != Direction.DOWN && direction != Direction.UP) return null
        val hanging = direction == Direction.UP
        return event.block.withProperty(States.HANGING, hanging.toString())
    }

    override fun canSurvive(state: Block, instance: Instance, position: BlockVec): Boolean {
        val hanging = state.getProperty(States.HANGING) == "true"
        val connected = if (hanging) Direction.DOWN else Direction.UP
        val support = position.relative(connected.opposite().blockFace)
        return Support.isFaceSturdy(instance, support, connected.blockFace)
    }
}
