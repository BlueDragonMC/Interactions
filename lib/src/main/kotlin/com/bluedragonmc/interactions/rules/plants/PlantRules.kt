package com.bluedragonmc.interactions.rules.plants

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.rules.base.BushPlacementRule
import com.bluedragonmc.interactions.rules.base.GrowingPlantPlacementRule
import com.bluedragonmc.interactions.rules.base.SegmentablePlacementRule
import com.bluedragonmc.interactions.rules.base.SpeleothemPlacementRule
import com.bluedragonmc.interactions.util.Fluids
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.Support
import com.bluedragonmc.interactions.util.Tags
import com.bluedragonmc.interactions.util.Waterlogged
import com.bluedragonmc.interactions.util.blockFace
import com.bluedragonmc.interactions.util.directionOf
import com.bluedragonmc.interactions.util.getBlockOrAir
import com.bluedragonmc.interactions.util.orderedDirections
import net.kyori.adventure.key.Key
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.utils.Direction

// --- Crops and ground plants -------------------------------------------------------------------

/** Crops that grow on farmland (`age` defaults to zero). */
class CropRule : BushPlacementRule(
    BlockSet.from(Block.BEETROOTS, Block.CARROTS, Block.POTATOES, Block.WHEAT, Block.TORCHFLOWER_CROP),
) {
    override fun mayPlaceOn(block: Block): Boolean = block.key() in Tags.SUPPORTS_CROPS
}

/** Melon and pumpkin stems, which also grow on farmland. */
class StemRule : BushPlacementRule(BlockSet.from(Block.MELON_STEM, Block.PUMPKIN_STEM)) {
    override fun mayPlaceOn(block: Block): Boolean = block.key() in Tags.SUPPORTS_STEM_CROPS
}

/** Nether wart grows on soul sand. */
class NetherWartRule : BushPlacementRule(BlockSet.from(Block.NETHER_WART)) {
    override fun mayPlaceOn(block: Block): Boolean = block.key() == Block.SOUL_SAND.key()
}

/** Saplings grow on vegetation-supporting blocks. */
class SaplingRule : BushPlacementRule(BlockSet.fromTag("minecraft:saplings"))

/** Flower beds (`pink_petals`, `wildflowers`) grow on vegetation and stack up to four flowers per block. */
class FlowerBedRule :
    SegmentablePlacementRule(BlockSet.from(Block.PINK_PETALS, Block.WILDFLOWERS), States.FLOWER_AMOUNT)

// --- Growing plants ----------------------------------------------------------------------------

/** Cactus grows upward on sand, but not next to a solid block or lava. */
class CactusRule : GrowingPlantPlacementRule(BlockSet.from(Block.CACTUS), Direction.UP) {
    override fun canAttachTo(block: Block): Boolean = block.key() in Tags.SUPPORTS_CACTUS

    override fun canSurvive(state: Block, instance: Instance, position: BlockVec): Boolean {
        for (face in HORIZONTAL) {
            val neighborPosition = position.relative(face)
            if (Support.isSolid(instance, neighborPosition)) return false
            if (instance.getBlockOrAir(neighborPosition).key().asString() == "minecraft:lava") return false
        }
        return super.canSurvive(state, instance, position)
    }

    private companion object {
        val HORIZONTAL = listOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)
    }
}

/** Sugar cane grows upward on sand/dirt next to water. */
class SugarCaneRule : GrowingPlantPlacementRule(BlockSet.from(Block.SUGAR_CANE), Direction.UP) {
    override fun canAttachTo(block: Block): Boolean = block.key() in Tags.SUPPORTS_SUGAR_CANE

    override fun canSurvive(state: Block, instance: Instance, position: BlockVec): Boolean {
        val belowPosition = position.relative(BlockFace.BOTTOM)
        val below = instance.getBlockOrAir(belowPosition)
        if (isPlant(below)) return true
        if (below.key() !in Tags.SUPPORTS_SUGAR_CANE) return false
        return BlockFace.entries.any { face ->
            val neighborPosition = belowPosition.relative(face)
            Fluids.isWater(instance, neighborPosition) ||
                instance.getBlockOrAir(neighborPosition).key() in Tags.SUPPORTS_SUGAR_CANE_ADJACENTLY
        }
    }
}

/** Bamboo grows upward on sand/dirt. */
class BambooRule : GrowingPlantPlacementRule(BlockSet.from(Block.BAMBOO), Direction.UP) {
    override fun canAttachTo(block: Block): Boolean = block.key() in Tags.SUPPORTS_BAMBOO
}

/** Kelp grows upward through water. */
class KelpRule : GrowingPlantPlacementRule(BlockSet.from(Block.KELP, Block.KELP_PLANT), Direction.UP) {
    override fun headBlock(): Block = Block.KELP
    override fun bodyBlock(): Block = Block.KELP_PLANT
    override fun requiresWater(): Boolean = true
}

/** Cave vines hang downward and grow berries. */
class CaveVinesRule :
    GrowingPlantPlacementRule(BlockSet.from(Block.CAVE_VINES, Block.CAVE_VINES_PLANT), Direction.DOWN) {
    override fun headBlock(): Block = Block.CAVE_VINES
    override fun bodyBlock(): Block = Block.CAVE_VINES_PLANT
}

/** Twisting vines grow upward. */
class TwistingVinesRule :
    GrowingPlantPlacementRule(BlockSet.from(Block.TWISTING_VINES, Block.TWISTING_VINES_PLANT), Direction.UP) {
    override fun headBlock(): Block = Block.TWISTING_VINES
    override fun bodyBlock(): Block = Block.TWISTING_VINES_PLANT
}

/** Weeping vines grow downward. */
class WeepingVinesRule :
    GrowingPlantPlacementRule(BlockSet.from(Block.WEEPING_VINES, Block.WEEPING_VINES_PLANT), Direction.DOWN) {
    override fun headBlock(): Block = Block.WEEPING_VINES
    override fun bodyBlock(): Block = Block.WEEPING_VINES_PLANT
}

// --- Water plants ------------------------------------------------------------------------------

/** Coral plants: waterlogged, require water and center support below. */
class CoralRule : PlacementRule(BlockSet.matching { it.endsWith("_coral") }) {

    init {
        onPlace {
            val below = blockPosition.relative(BlockFace.BOTTOM)
            if (!Fluids.isWater(instance, blockPosition) || !Support.isFaceFull(instance, below, BlockFace.TOP)) {
                isCancelled = true
                return@onPlace
            }
            block = Waterlogged.apply(block, true)
        }

        onUpdate {
            if (!Fluids.isWater(instance, blockPosition)) destroy()
        }
    }
}

/** Coral fans: standing on top, wall-mounted otherwise, and always in water. */
class CoralFanRule : com.bluedragonmc.interactions.rules.base.StandingAndWallPlacementRule(
    BlockSet.matching { it.endsWith("_coral_fan") },
    BlockSet.matching { it.endsWith("_coral_wall_fan") },
    Direction.DOWN,
    ::wallFanVariant,
) {
    override fun canSurviveStanding(state: Block, instance: Instance, position: BlockVec): Boolean =
        Fluids.isWater(instance, position) &&
            Support.isFaceFull(instance, position.relative(BlockFace.BOTTOM), BlockFace.TOP)

    override fun canSurviveWall(
        state: Block,
        supportDirection: Direction,
        instance: Instance,
        position: BlockVec,
    ): Boolean = Fluids.isWater(instance, position) && super.canSurviveWall(state, supportDirection, instance, position)
}

private fun wallFanVariant(block: Block): Block? {
    val path = block.key().value()
    if (path.endsWith("_coral_wall_fan")) return block
    if (!path.endsWith("_coral_fan")) return null
    return Block.fromKey(Key.key(block.key().namespace(), path.removeSuffix("_coral_fan") + "_coral_wall_fan").asString())
}

// --- Dripstone and sulfur ----------------------------------------------------------------------

/** Pointed dripstone points away from the player's vertical look and connects to its neighbours. */
class PointedDripstoneRule : SpeleothemPlacementRule(BlockSet.from(Block.POINTED_DRIPSTONE))

/** Sulfur spikes point away from the player's vertical look and connect to their neighbours. */
class SulfurSpikeRule : SpeleothemPlacementRule(BlockSet.from(Block.SULFUR_SPIKE))

/** Sweet berry bushes grow on vegetation-supporting blocks. */
class SweetBerryBushRule : BushPlacementRule(BlockSet.from(Block.SWEET_BERRY_BUSH))

/** Vines attach to any neighbouring face except below. */
class VineRule : com.bluedragonmc.interactions.rules.base.MultifacePlacementRule(
    BlockSet.from(Block.VINE),
    listOf(Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST),
    applyWaterlogged = false,
)

/** Glow lichen, sculk veins and resin clumps attach to any neighbouring face and carry `waterlogged`. */
class MultifaceVeinRule : com.bluedragonmc.interactions.rules.base.MultifacePlacementRule(
    BlockSet.matching {
        it == "minecraft:glow_lichen" || it == "minecraft:sculk_vein" || it == "minecraft:resin_clump"
    },
    Direction.entries.toList(),
    applyWaterlogged = true,
)

/** Cocoa pods attach to the side of a jungle log, facing that log. */
class CocoaRule : PlacementRule(BlockSet.from(Block.COCOA)) {
    init {
        onPlace {
            isCancelled = true
            for (direction in player.position.orderedDirections(blockFace.oppositeFace.toDirection())) {
                if (!direction.horizontal()) continue
                if (!isJungleLog(instance.getBlockOrAir(blockPosition.relative(direction.blockFace)))) continue
                block = block.withProperty(States.FACING, direction.name.lowercase())
                isCancelled = false
                return@onPlace
            }
        }

        onUpdate {
            val facing = directionOf(block.getProperty(States.FACING)!!)
            if (!isJungleLog(instance.getBlockOrAir(blockPosition.relative(facing.blockFace)))) destroy()
        }
    }

    private fun isJungleLog(block: Block): Boolean {
        val key = block.key().asString()
        return key == "minecraft:jungle_log" || key == "minecraft:jungle_wood" ||
            key == "minecraft:stripped_jungle_log" || key == "minecraft:stripped_jungle_wood"
    }
}
