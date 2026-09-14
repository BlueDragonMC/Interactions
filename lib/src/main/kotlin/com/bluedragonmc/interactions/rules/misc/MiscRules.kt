package com.bluedragonmc.interactions.rules.misc

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.event.BlockPlacementEvent
import com.bluedragonmc.interactions.rules.base.CandleRule
import com.bluedragonmc.interactions.rules.base.ClickFacingRule
import com.bluedragonmc.interactions.rules.base.DirectionalPlacementRule
import com.bluedragonmc.interactions.rules.base.FaceAttachedPlacementRule
import com.bluedragonmc.interactions.rules.base.HorizontalFacingRule
import com.bluedragonmc.interactions.rules.base.NearestFacingRule
import com.bluedragonmc.interactions.rules.base.SeaPickleRule
import com.bluedragonmc.interactions.rules.base.SnowLayerRule
import com.bluedragonmc.interactions.rules.base.TurtleEggRule
import com.bluedragonmc.interactions.util.Signals
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.Support
import com.bluedragonmc.interactions.util.Waterlogged
import com.bluedragonmc.interactions.util.clockWise
import com.bluedragonmc.interactions.util.getBlockOrAir
import com.bluedragonmc.interactions.util.horizontalDirection
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.utils.Direction

// --- Containers --------------------------------------------------------------------------------

/** Chests (and copper chests) face the player. */
class ChestRule : HorizontalFacingRule(
    BlockSet.matching { it == "minecraft:chest" || (it.endsWith("_chest") && it != "minecraft:ender_chest") },
)

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

/** Candles stack up to four per block. */
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

    private fun distance(instance: net.minestom.server.instance.Instance, position: BlockVec): Int {
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

/** Leaf litter faces the player's horizontal look. */
class LeafLitterRule : HorizontalFacingRule(BlockSet.from(Block.LEAF_LITTER), facesPlayer = false)

