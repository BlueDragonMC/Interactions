package com.bluedragonmc.interactions.rules.multiblock

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.event.BlockPlacementEvent
import com.bluedragonmc.interactions.rules.base.HorizontalFacingRule
import com.bluedragonmc.interactions.rules.base.MultiBlockPlacementRule
import com.bluedragonmc.interactions.util.*
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.utils.Direction

/** Doors: `facing` from the player, `hinge` from the surrounding blocks, and a two-block tall pair. */
class DoorRule : MultiBlockPlacementRule(BlockSet.fromTag("minecraft:doors")) {

    override fun primaryState(event: BlockPlacementEvent): Block {
        val facing = event.player.position.horizontalDirection()
        val powered = Signals.hasNeighborSignal(event.instance, event.blockPosition) ||
            Signals.hasNeighborSignal(event.instance, event.blockPosition.relative(BlockFace.TOP))
        return event.block
            .withProperty(States.FACING, facing.name.lowercase())
            .withProperty(States.HINGE, hinge(facing, event))
            .withProperty(States.POWERED, powered.toString())
            .withProperty(States.OPEN, powered.toString())
            .withProperty(States.HALF, "lower")
    }

    private fun hinge(facing: BlockFace, event: BlockPlacementEvent): String {
        val level = event.instance
        val placeDirection = facing.toDirection()
        val position = event.blockPosition
        val abovePosition = position.relative(BlockFace.TOP)

        val leftDirection = placeDirection.counterClockWise()
        val leftPosition = position.relative(leftDirection.blockFace)
        val leftAbovePosition = abovePosition.relative(leftDirection.blockFace)

        val rightDirection = placeDirection.clockWise()
        val rightPosition = position.relative(rightDirection.blockFace)
        val rightAbovePosition = abovePosition.relative(rightDirection.blockFace)

        val balance =
            (if (Support.isFullBlock(level.getBlockOrAir(leftPosition))) -1 else 0) +
                (if (Support.isFullBlock(level.getBlockOrAir(leftAbovePosition))) -1 else 0) +
                (if (Support.isFullBlock(level.getBlockOrAir(rightPosition))) 1 else 0) +
                (if (Support.isFullBlock(level.getBlockOrAir(rightAbovePosition))) 1 else 0)

        val doorLeft = isLowerDoor(level.getBlockOrAir(leftPosition))
        val doorRight = isLowerDoor(level.getBlockOrAir(rightPosition))

        if ((!doorLeft || doorRight) && balance <= 0) {
            if ((!doorRight || doorLeft) && balance >= 0) {
                return if (clickPrefersLeft(placeDirection, event)) "left" else "right"
            }
            return "left"
        }
        return "right"
    }

    private fun isLowerDoor(block: Block): Boolean =
        block.key() in keys && block.getProperty(States.HALF) == "lower"

    /** Breaks an even support balance by choosing the hinge side from where the player clicked. */
    private fun clickPrefersLeft(placeDirection: Direction, event: BlockPlacementEvent): Boolean {
        val stepX = placeDirection.normalX()
        val stepZ = placeDirection.normalZ()
        val faceNormal = event.blockFace.toDirection()
        val clickX = event.cursorPosition.x() - faceNormal.normalX()
        val clickZ = event.cursorPosition.z() - faceNormal.normalZ()
        return (stepX >= 0 || !(clickZ < 0.5)) &&
            (stepX <= 0 || !(clickZ > 0.5)) &&
            (stepZ >= 0 || !(clickX > 0.5)) &&
            (stepZ <= 0 || !(clickX < 0.5))
    }

    override fun secondaryPosition(primary: Block, event: BlockPlacementEvent): BlockVec =
        event.blockPosition.relative(BlockFace.TOP)

    override fun secondaryState(primary: Block, event: BlockPlacementEvent): Block =
        primary.withProperty(States.HALF, "upper")

    override fun pairPosition(broken: Block, position: BlockVec): BlockVec =
        if (broken.getProperty(States.HALF) == "lower") position.relative(BlockFace.TOP)
        else position.relative(BlockFace.BOTTOM)
}

/** Beds: the clicked position is the foot, and the head is placed in the facing direction. */
class BedRule : MultiBlockPlacementRule(BlockSet.fromTag("minecraft:beds")) {

    override fun primaryState(event: BlockPlacementEvent): Block =
        event.block
            .withProperty(States.FACING, event.player.position.horizontalDirection().name.lowercase())
            .withProperty(States.PART, "foot")

    override fun secondaryPosition(primary: Block, event: BlockPlacementEvent): BlockVec {
        val facing = directionOf(primary.getProperty(States.FACING)!!)
        return event.blockPosition.relative(facing.blockFace)
    }

    override fun secondaryState(primary: Block, event: BlockPlacementEvent): Block =
        primary.withProperty(States.PART, "head")

    override fun pairPosition(broken: Block, position: BlockVec): BlockVec {
        val facing = directionOf(broken.getProperty(States.FACING)!!)
        return if (broken.getProperty(States.PART) == "foot") position.relative(facing.blockFace)
        else position.relative(facing.opposite().blockFace)
    }
}

/** Tall flowers and grass: a lower and an upper half placed together. */
class TallPlantRule : MultiBlockPlacementRule(
    BlockSet.matching {
        it == "minecraft:sunflower" || it == "minecraft:lilac" || it == "minecraft:rose_bush" ||
            it == "minecraft:peony" || it == "minecraft:tall_grass" || it == "minecraft:large_fern" ||
            it == "minecraft:tall_seagrass"
    },
) {
    override fun primaryState(event: BlockPlacementEvent): Block = event.block.withProperty(States.HALF, "lower")

    override fun secondaryPosition(primary: Block, event: BlockPlacementEvent): BlockVec =
        event.blockPosition.relative(BlockFace.TOP)

    override fun secondaryState(primary: Block, event: BlockPlacementEvent): Block =
        primary.withProperty(States.HALF, "upper")

    override fun pairPosition(broken: Block, position: BlockVec): BlockVec =
        if (broken.getProperty(States.HALF) == "lower") position.relative(BlockFace.TOP)
        else position.relative(BlockFace.BOTTOM)
}

/** Pitcher crops grow as a lower and upper block. */
class PitcherCropRule : MultiBlockPlacementRule(BlockSet.from(Block.PITCHER_CROP, Block.PITCHER_PLANT)) {
    override fun primaryState(event: BlockPlacementEvent): Block = event.block.withProperty(States.HALF, "lower")

    override fun secondaryPosition(primary: Block, event: BlockPlacementEvent): BlockVec =
        event.blockPosition.relative(BlockFace.TOP)

    override fun secondaryState(primary: Block, event: BlockPlacementEvent): Block =
        primary.withProperty(States.HALF, "upper")

    override fun pairPosition(broken: Block, position: BlockVec): BlockVec =
        if (broken.getProperty(States.HALF) == "lower") position.relative(BlockFace.TOP)
        else position.relative(BlockFace.BOTTOM)
}

/** Big dripleaf (and its stem) face the player and carry `waterlogged`. */
class BigDripleafRule : HorizontalFacingRule(
    BlockSet.from(Block.BIG_DRIPLEAF, Block.BIG_DRIPLEAF_STEM),
    facesPlayer = false,
)

/** Small dripleaf is a two-block structure facing the player. */
class SmallDripleafRule : MultiBlockPlacementRule(BlockSet.from(Block.SMALL_DRIPLEAF)) {
    override fun primaryState(event: BlockPlacementEvent): Block =
        event.block
            .withProperty(States.FACING, event.player.position.horizontalDirection().name.lowercase())
            .withProperty(States.HALF, "lower")

    override fun secondaryPosition(primary: Block, event: BlockPlacementEvent): BlockVec =
        event.blockPosition.relative(BlockFace.TOP)

    override fun secondaryState(primary: Block, event: BlockPlacementEvent): Block =
        primary.withProperty(States.HALF, "upper")

    override fun pairPosition(broken: Block, position: BlockVec): BlockVec =
        if (broken.getProperty(States.HALF) == "lower") position.relative(BlockFace.TOP)
        else position.relative(BlockFace.BOTTOM)
}
