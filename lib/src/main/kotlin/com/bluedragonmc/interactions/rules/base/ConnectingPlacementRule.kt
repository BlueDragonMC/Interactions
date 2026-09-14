package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.Support
import com.bluedragonmc.interactions.util.Waterlogged
import com.bluedragonmc.interactions.util.getBlockOrAir
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.utils.Direction

/**
 * Support for cross-connecting blocks, like fences, walls, iron bars, glass panes, and chains.
 */
abstract class ConnectingPlacementRule(
    blocks: BlockSet,
    private val connectionFaces: List<BlockFace> = HORIZONTAL,
) : PlacementRule(blocks) {

    init {
        onPlace {
            block = computeConnections(block, instance, blockPosition)
        }

        onUpdate {
            if (fromFace !in connectionFaces) return@onUpdate
            val connected = connects(instance, blockPosition, fromFace)
            resultBlock = resultBlock.withProperty(States.connection(fromFace), connected.toString())
        }
    }

    /** Connections only need updating on the changed block and its immediate neighbors. */
    override fun maxUpdateDistance(): Int = 1

    /**
     * Whether this block connects to [neighbor].
     *
     * @param faceSolid whether the neighbor's face toward this block is a full shape.
     * @param direction the direction from the neighbor back toward this block.
     */
    protected abstract fun canConnect(neighbor: Block, faceSolid: Boolean, direction: Direction): Boolean

    private fun computeConnections(state: Block, instance: Instance, position: BlockVec): Block {
        var result = state
        for (face in connectionFaces) {
            result = result.withProperty(States.connection(face), connects(instance, position, face).toString())
        }
        return Waterlogged.apply(result, instance, position)
    }

    private fun connects(instance: Instance, position: BlockVec, face: BlockFace): Boolean {
        val neighborPosition = position.relative(face)
        val neighbor = instance.getBlockOrAir(neighborPosition)
        val faceSolid = Support.isFaceSturdy(instance, neighborPosition, face.oppositeFace)
        return canConnect(neighbor, faceSolid, face.oppositeFace.toDirection())
    }

    companion object {
        val HORIZONTAL: List<BlockFace> = listOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)
        val ALL: List<BlockFace> = BlockFace.entries.toList()
    }
}
