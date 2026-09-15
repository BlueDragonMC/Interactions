package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.event.BlockPlacementEvent
import com.bluedragonmc.interactions.plus
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.Support
import com.bluedragonmc.interactions.util.Waterlogged
import com.bluedragonmc.interactions.util.blockFace
import com.bluedragonmc.interactions.util.directionOf
import com.bluedragonmc.interactions.util.orderedDirections
import net.kyori.adventure.key.Key
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.utils.Direction

/**
 * Base for items that place either a standing or a wall-mounted block: signs, banners, skulls,
 * torches and coral fans.
 *
 * The held item maps to the *standing* block, so a rule registered only for the wall block would
 * never run. This base registers for both.
 *
 * @param standingBlocks the item/standing blocks (used to match placement).
 * @param wallBlocks the wall blocks (matched so updates on existing wall blocks are handled).
 * @param standingDirection the direction that selects the standing variant (`DOWN` for most, `UP`
 *   for ceiling hanging signs).
 * @param wallVariant maps a standing block to its wall variant.
 */
abstract class StandingAndWallPlacementRule(
    standingBlocks: BlockSet,
    private val wallBlocks: BlockSet,
    private val standingDirection: Direction,
    private val wallVariant: (Block) -> Block?,
) : PlacementRule(standingBlocks + wallBlocks) {

    private val wallByStanding: Map<Key, Block> by lazy {
        standingBlocks.getBlocks().mapNotNull { standing ->
            wallVariant(standing)?.let { standing.key() to it }
        }.toMap()
    }

    init {
        onPlace {
            isCancelled = true
            val standing = block
            val wall = wallByStanding[standing.key()]
            for (direction in player.position.orderedDirections(blockFace.oppositeFace.toDirection())) {
                if (direction == standingDirection.opposite()) continue
                val candidate: Block
                val survives: Boolean
                if (direction == standingDirection) {
                    candidate = standingState(standing, this)
                    survives = canSurviveStanding(candidate, instance, blockPosition)
                } else {
                    if (!direction.horizontal() || wall == null) continue
                    candidate = wallState(wall, direction, this)
                    survives = canSurviveWall(candidate, direction, instance, blockPosition)
                }
                if (survives) {
                    block = Waterlogged.apply(candidate, instance, blockPosition)
                    isCancelled = false
                    return@onPlace
                }
            }
        }

        onUpdate {
            if (block.key() in wallBlocks.keys) {
                val facing = directionOf(block.getProperty(States.FACING)!!)
                if (!canSurviveWall(block, facing.opposite(), instance, blockPosition)) destroy()
            } else if (!canSurviveStanding(block, instance, blockPosition)) {
                destroy()
            }
        }
    }

    /** The state for the standing variant. Defaults to the held block. */
    protected open fun standingState(standing: Block, event: BlockPlacementEvent): Block = standing

    /** The state for the wall variant mounted toward [direction]. */
    protected open fun wallState(wall: Block, direction: Direction, event: BlockPlacementEvent): Block =
        wall.withProperty(States.FACING, direction.opposite().name.lowercase())

    /** Whether the standing state can stay at [position]. */
    protected open fun canSurviveStanding(state: Block, instance: Instance, position: BlockVec): Boolean = true

    /** Whether the wall state can stay, where [supportDirection] points from the block to its support. */
    protected open fun canSurviveWall(
        state: Block,
        supportDirection: Direction,
        instance: Instance,
        position: BlockVec,
    ): Boolean {
        val support = position.relative(supportDirection.blockFace)
        return Support.isFaceSturdy(instance, support, supportDirection.opposite().blockFace)
    }
}
