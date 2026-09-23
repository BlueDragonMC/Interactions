package com.bluedragonmc.interactions.util

import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace

/**
 * Shared redstone-wire connection logic, used by both the placement rule and the reshape interaction.
 */
object RedstoneWire {

    /** Recomputes the `north`/`east`/`south`/`west` connection properties of [base]. */
    fun withConnections(base: Block, instance: Instance, position: BlockVec, preserveDot: Boolean): Block {
        val north = computeSide(instance, position, BlockFace.NORTH)
        val east = computeSide(instance, position, BlockFace.EAST)
        val south = computeSide(instance, position, BlockFace.SOUTH)
        val west = computeSide(instance, position, BlockFace.WEST)

        val northConnected = north != "none"
        val southConnected = south != "none"
        val eastConnected = east != "none"
        val westConnected = west != "none"

        if (preserveDot && !northConnected && !southConnected && !eastConnected && !westConnected) {
            return base
                .withProperty(States.NORTH, "none")
                .withProperty(States.EAST, "none")
                .withProperty(States.SOUTH, "none")
                .withProperty(States.WEST, "none")
        }

        val northSouthEmpty = !northConnected && !southConnected
        val eastWestEmpty = !eastConnected && !westConnected
        val resolvedNorth = if (!northConnected && eastWestEmpty) "side" else north
        val resolvedSouth = if (!southConnected && eastWestEmpty) "side" else south
        val resolvedEast = if (!eastConnected && northSouthEmpty) "side" else east
        val resolvedWest = if (!westConnected && northSouthEmpty) "side" else west

        return base
            .withProperty(States.NORTH, resolvedNorth)
            .withProperty(States.EAST, resolvedEast)
            .withProperty(States.SOUTH, resolvedSouth)
            .withProperty(States.WEST, resolvedWest)
    }

    fun isCross(block: Block): Boolean =
        connected(block, States.NORTH) && connected(block, States.EAST) &&
            connected(block, States.SOUTH) && connected(block, States.WEST)

    fun isDot(block: Block): Boolean =
        !connected(block, States.NORTH) && !connected(block, States.EAST) &&
            !connected(block, States.SOUTH) && !connected(block, States.WEST)

    /** Whether the block below [position] can support the wire. */
    fun canSurvive(instance: Instance, position: BlockVec): Boolean =
        Support.canSupportCenter(instance, position.relative(BlockFace.BOTTOM), BlockFace.TOP)

    private fun connected(block: Block, property: String): Boolean = block.getProperty(property) != "none"

    private fun computeSide(instance: Instance, position: BlockVec, face: BlockFace): String {
        val sidePosition = position.relative(face)
        val sideBlock = instance.getBlockOrAir(sidePosition)

        if (sideBlock.key() == Block.REDSTONE_WIRE.key()) return "side"

        val aboveBlock = instance.getBlockOrAir(position.relative(BlockFace.TOP))
        val aboveSideBlock = instance.getBlockOrAir(sidePosition.relative(BlockFace.TOP))
        val sideSturdyTop = Support.isFaceFull(sideBlock, BlockFace.TOP)
        val aboveSolid = Support.isFaceFull(aboveBlock, BlockFace.BOTTOM)

        if (sideSturdyTop && !aboveSolid && aboveSideBlock.key() == Block.REDSTONE_WIRE.key()) return "up"

        val belowSideBlock = instance.getBlockOrAir(sidePosition.relative(BlockFace.BOTTOM))
        if (!sideSturdyTop && belowSideBlock.key() == Block.REDSTONE_WIRE.key()) return "side"

        return "none"
    }
}
