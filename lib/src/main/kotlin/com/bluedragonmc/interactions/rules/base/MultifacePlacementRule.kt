package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.Support
import com.bluedragonmc.interactions.util.Waterlogged
import com.bluedragonmc.interactions.util.blockFace
import com.bluedragonmc.interactions.util.getBlockOrAir
import com.bluedragonmc.interactions.util.orderedDirections
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.utils.Direction

/**
 * Base for multi-face blocks (vines, glow lichen, sculk veins, resin clumps): placement picks the
 * first face the player is looking at that can be attached to, and updates recompute every face.
 *
 * @param faces the directions this block can attach to.
 * @param applyWaterlogged whether the block has a `waterlogged` property to derive.
 */
abstract class MultifacePlacementRule(
    blocks: BlockSet,
    private val faces: List<Direction>,
    private val applyWaterlogged: Boolean,
) : PlacementRule(blocks) {

    init {
        onPlace {
            isCancelled = true
            for (direction in player.position.orderedDirections()) {
                if (direction !in faces) continue
                if (!canAttach(instance, blockPosition, direction)) continue
                var state = block.withProperty(property(direction), "true")
                if (applyWaterlogged) state = Waterlogged.apply(state, instance, blockPosition)
                block = state
                isCancelled = false
                return@onPlace
            }
        }

        onUpdate {
            resultBlock = update(resultBlock, instance, blockPosition)
        }
    }

    private fun update(state: Block, instance: Instance, position: BlockVec): Block {
        var result = state
        var any = false
        for (direction in faces) {
            val connected = canAttach(instance, position, direction)
            if (connected) any = true
            result = result.withProperty(property(direction), connected.toString())
        }
        if (!any) return Block.AIR
        return if (applyWaterlogged) Waterlogged.apply(result, instance, position) else result
    }

    private fun canAttach(instance: Instance, position: BlockVec, direction: Direction): Boolean {
        val neighborPosition = position.relative(direction.blockFace)
        val neighbor = instance.getBlockOrAir(neighborPosition)
        if (neighbor.key() in keys) return true
        return Support.isFaceSturdy(instance, neighborPosition, direction.opposite().blockFace)
    }

    private fun property(direction: Direction): String = States.connection(direction.blockFace)
}
