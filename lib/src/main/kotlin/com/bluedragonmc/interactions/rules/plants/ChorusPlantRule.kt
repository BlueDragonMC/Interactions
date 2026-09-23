package com.bluedragonmc.interactions.rules.plants

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.Tags
import com.bluedragonmc.interactions.util.getBlockOrAir
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace

/** Chorus plants and flowers connect to each other and to a supporting block below. */
class ChorusPlantRule : PlacementRule(BlockSet.from(Block.CHORUS_PLANT, Block.CHORUS_FLOWER)) {
    init {
        onPlace {
            if (!hasSupport(instance, blockPosition)) {
                isCancelled = true
                return@onPlace
            }
            block = withConnections(instance, blockPosition, block)
        }

        onUpdate {
            if (!hasSupport(instance, blockPosition)) {
                destroy()
                return@onUpdate
            }
            resultBlock = withConnections(instance, blockPosition, block)
        }
    }
}

private fun hasSupport(instance: Instance, position: BlockVec): Boolean =
    BlockFace.entries.any { connects(instance, position, it) }

private fun connects(instance: Instance, position: BlockVec, face: BlockFace): Boolean {
    val neighbor = instance.getBlockOrAir(position.relative(face))
    if (neighbor.key() == Block.CHORUS_PLANT.key() || neighbor.key() == Block.CHORUS_FLOWER.key()) return true
    return face == BlockFace.BOTTOM && neighbor.key() in Tags.SUPPORTS_CHORUS_PLANT
}

private fun withConnections(instance: Instance, position: BlockVec, block: Block): Block = block
    .withProperty(States.DOWN, connects(instance, position, BlockFace.BOTTOM).toString())
    .withProperty(States.UP, connects(instance, position, BlockFace.TOP).toString())
    .withProperty(States.NORTH, connects(instance, position, BlockFace.NORTH).toString())
    .withProperty(States.EAST, connects(instance, position, BlockFace.EAST).toString())
    .withProperty(States.SOUTH, connects(instance, position, BlockFace.SOUTH).toString())
    .withProperty(States.WEST, connects(instance, position, BlockFace.WEST).toString())
