package com.bluedragonmc.interactions.rules.misc

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.event.BlockPlacementEvent
import com.bluedragonmc.interactions.rules.base.*
import com.bluedragonmc.interactions.util.*
import net.kyori.adventure.key.Key
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.item.Material
import net.minestom.server.utils.Direction

// --- Containers --------------------------------------------------------------------------------

/** Chests (and copper chests) face the player and pair with an adjacent chest into a double chest. */
class ChestRule : PlacementRule(
    BlockSet.matching { it == "minecraft:chest" || (it.endsWith("_chest") && it != "minecraft:ender_chest") },
) {
    init {
        onPlace {
            val clickedFace = blockFace
            val clickedDirection = clickedFace.oppositeFace.toDirection()
            var facingDirection = player.position.horizontalDirection().oppositeFace.toDirection()
            var type = "single"
            if (clickedFace.toDirection().horizontal() && context.isSneaking) {
                val neighbourFacing = candidatePartnerFacing(instance, blockPosition, clickedDirection, block.key())
                if (neighbourFacing != null && neighbourFacing.axisName != clickedFace.axisName) {
                    facingDirection = neighbourFacing
                    type = if (facingDirection.counterClockWise() == clickedDirection) "right" else "left"
                }
            }
            if (type == "single" && !context.isSneaking) {
                type = chestType(instance, blockPosition, facingDirection, block.key())
            }
            block = Waterlogged.apply(
                block
                    .withProperty(States.FACING, facingDirection.name.lowercase())
                    .withProperty(States.TYPE, type),
                instance,
                blockPosition,
            )
        }

        onUpdate {
            val direction = fromFace.toDirection()
            val neighbor = if (direction.horizontal()) instance.getBlockOrAir(blockPosition.relative(fromFace)) else null
            if (neighbor != null && neighbor.key() == block.key()) {
                val neighbourType = neighbor.getProperty(States.TYPE)
                if (block.getProperty(States.TYPE) == "single" &&
                    neighbourType != "single" &&
                    block.getProperty(States.FACING) == neighbor.getProperty(States.FACING) &&
                    connectedDirection(neighbor) == direction.opposite()
                ) {
                    resultBlock = block.withProperty(States.TYPE, if (neighbourType == "left") "right" else "left")
                    return@onUpdate
                }
            } else if (connectedDirection(block) == direction) {
                resultBlock = block.withProperty(States.TYPE, "single")
            }
        }
    }

    private fun connectedDirection(state: Block): Direction {
        val facing = directionOf(state.getProperty(States.FACING)!!)
        return if (state.getProperty(States.TYPE) == "left") facing.clockWise() else facing.counterClockWise()
    }

    private fun candidatePartnerFacing(
        instance: Instance,
        position: BlockVec,
        neighbourDirection: Direction,
        key: Key,
    ): Direction? {
        val state = instance.getBlockOrAir(position.relative(neighbourDirection.blockFace))
        if (state.key() != key || state.getProperty(States.TYPE) != "single") return null
        return state.getProperty(States.FACING)?.let(::directionOf)
    }

    private fun chestType(
        instance: Instance,
        position: BlockVec,
        facingDirection: Direction,
        key: Key,
    ): String {
        if (facingDirection == candidatePartnerFacing(instance, position, facingDirection.clockWise(), key)) {
            return "left"
        }
        return if (facingDirection == candidatePartnerFacing(instance, position, facingDirection.counterClockWise(), key)) {
            "right"
        } else {
            "single"
        }
    }
}

/** Shulker boxes face the clicked face. */
class ShulkerBoxRule : ClickFacingRule(BlockSet.fromTag("minecraft:shulker_boxes"))

/** Hoppers face away from the clicked face, defaulting to down for vertical faces, and start enabled. */
class HopperRule : PlacementRule(BlockSet.from(Block.HOPPER)) {
    init {
        onPlace {
            val direction = blockFace.toDirection().opposite()
            val facing = if (direction.vertical()) Direction.DOWN else direction
            block = block
                .withProperty(States.FACING, facing.name.lowercase())
                .withProperty(States.ENABLED, "true")
        }
    }
}

// --- Stacking single blocks --------------------------------------------------------------------

/** Candles stack up to four per block and can be lit with flint and steel or a fire charge. */
class CandlePlacementRule : CandleRule(BlockSet.fromTag("minecraft:candles"))

/** Snow layers stack only when clicked from above. */
class SnowRule : SnowLayerRule(BlockSet.from(Block.SNOW))

/** Sea pickles stack and waterlog. */
class SeaPicklePlacementRule : SeaPickleRule(BlockSet.from(Block.SEA_PICKLE))

/** Turtle eggs stack up to four per block. */
class TurtleEggPlacementRule : TurtleEggRule(BlockSet.from(Block.TURTLE_EGG))

// --- Workstations and misc ---------------------------------------------------------------------

/** Anvils face clockwise from the player's look. */
class AnvilRule : DirectionalPlacementRule(BlockSet.fromTag("minecraft:anvil")) {
    override fun placementState(event: BlockPlacementEvent): Block {
        val facing = event.player.position.horizontalDirection().toDirection().clockWise()
        return event.block.withProperty(States.FACING, facing.name.lowercase())
    }
}

/** Grindstones attach to a face like buttons and levers. */
class GrindstoneRule : FaceAttachedPlacementRule(BlockSet.from(Block.GRINDSTONE))

/** Campfires face the player and light unless placed in water. */
class CampfireRule : HorizontalFacingRule(
    BlockSet.from(Block.CAMPFIRE, Block.SOUL_CAMPFIRE),
    facesPlayer = false,
)

/** Shelves face the player and follow redstone signals. */
class ShelfRule : PlacementRule(BlockSet.matching { it.endsWith("_shelf") }) {
    init {
        onPlace {
            val facing = player.position.horizontalDirection().oppositeFace
            val powered = Signals.hasNeighborSignal(instance, blockPosition)
            block = Waterlogged.apply(
                block
                    .withProperty(States.FACING, facing.name.lowercase())
                    .withProperty(States.POWERED, powered.toString()),
                instance,
                blockPosition,
            )
        }
    }
}

/** Workstations that simply face the player. */
class FacingWorkstationRule : HorizontalFacingRule(
    BlockSet.from(
        Block.LECTERN,
        Block.LOOM,
        Block.STONECUTTER,
        Block.VAULT,
        Block.CHISELED_BOOKSHELF,
        Block.DRIED_GHAST,
    ),
)

/** Scaffolding tracks its distance to the nearest support and whether it is the bottom piece. */
class ScaffoldingRule : PlacementRule(BlockSet.from(Block.SCAFFOLDING)) {
    init {
        onPlace {
            val distance = distance(instance, blockPosition)
            if (distance >= 7) {
                isCancelled = true
                return@onPlace
            }
            block = Waterlogged.apply(
                block
                    .withProperty(States.DISTANCE, distance.toString())
                    .withProperty(States.BOTTOM, (distance > 0 && instance.getBlockOrAir(blockPosition.relative(BlockFace.BOTTOM)).key() != Block.SCAFFOLDING.key()).toString()),
                instance,
                blockPosition,
            )
        }

        onUpdate {
            val distance = distance(instance, blockPosition)
            if (distance >= 7) {
                destroy()
            } else {
                resultBlock = resultBlock.withProperty(States.DISTANCE, distance.toString())
            }
        }
    }

    override fun maxUpdateDistance(): Int = 8

    private fun distance(instance: Instance, position: BlockVec): Int {
        val belowPosition = position.relative(BlockFace.BOTTOM)
        val below = instance.getBlockOrAir(belowPosition)
        var distance = 7
        if (below.key() == Block.SCAFFOLDING.key()) {
            distance = below.getProperty(States.DISTANCE)?.toIntOrNull() ?: 7
        } else if (Support.isFaceFull(instance, belowPosition, BlockFace.TOP)) {
            return 0
        }

        for (face in listOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)) {
            val neighbor = instance.getBlockOrAir(position.relative(face))
            if (neighbor.key() == Block.SCAFFOLDING.key()) {
                val neighborDistance = neighbor.getProperty(States.DISTANCE)?.toIntOrNull() ?: 7
                distance = minOf(distance, neighborDistance + 1)
                if (distance == 1) break
            }
        }
        return distance
    }
}

// --- Miscellaneous stateful one-offs -----------------------------------------------------------

/** Wool carpets need a non-empty block below them and are removed when it disappears. */
class CarpetRule : PlacementRule(BlockSet.fromTag("minecraft:wool_carpets")) {
    init {
        onPlace {
            if (!hasSupport(instance, blockPosition)) isCancelled = true
        }

        onUpdate {
            if (fromFace == BlockFace.BOTTOM && !hasSupport(instance, blockPosition)) destroy()
        }
    }

    private fun hasSupport(instance: Instance, position: BlockVec): Boolean =
        !instance.getBlockOrAir(position.relative(BlockFace.BOTTOM)).air()
}

/** Blocks that only track `waterlogged` (barriers, conduits, grates, hanging roots, heavy cores). */
class WaterloggedRule : PlacementRule(
    BlockSet.matching {
        it == "minecraft:barrier" || it == "minecraft:conduit" || it == "minecraft:heavy_core" ||
            it == "minecraft:hanging_roots" || it.endsWith("copper_grate")
    },
) {
    init {
        onPlace { block = Waterlogged.apply(block, instance, blockPosition) }
    }
}

/** Command blocks face away from the player's nearest looking direction. */
class CommandBlockRule : NearestFacingRule(
    BlockSet.from(Block.COMMAND_BLOCK, Block.CHAIN_COMMAND_BLOCK, Block.REPEATING_COMMAND_BLOCK),
)

/** Calibrated sculk sensors face away from the player. */
class CalibratedSculkSensorRule : HorizontalFacingRule(BlockSet.from(Block.CALIBRATED_SCULK_SENSOR))

/** Leaf litter sits on any sturdy top face and stacks up to four segments per block. */
class LeafLitterRule : SegmentablePlacementRule(BlockSet.from(Block.LEAF_LITTER), States.SEGMENT_AMOUNT) {
    override fun mayPlaceOn(block: Block): Boolean = Support.isFaceFull(block, BlockFace.TOP)
}

