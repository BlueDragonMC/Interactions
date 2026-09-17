package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.event.BlockPlacementEvent
import com.bluedragonmc.interactions.util.Fluids
import com.bluedragonmc.interactions.util.Support
import com.bluedragonmc.interactions.util.blockFace
import com.bluedragonmc.interactions.util.getBlockOrAir
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.utils.Direction

/**
 * Base for growing plants: kelp, sugar cane, cactus, bamboo, chorus, cave vines, and twisting/weeping vines.
 *
 * The plant grows in
 * [growthDirection]; the segment adjacent to an existing head or body becomes a body block, while a
 * free position becomes the head block. Updates convert a head to a body when a segment is placed
 * ahead of it, and destroy the plant when its support disappears.
 */
abstract class GrowingPlantPlacementRule(
    blocks: BlockSet,
    protected val growthDirection: Direction,
) : PlacementRule(blocks) {

    /** The head block (the placed block with an age), defaulting to the set's default block. */
    protected open fun headBlock(): Block = blocks.getBlocks().first().defaultState()

    /** The body/segment block, defaulting to the head for single-block plants. */
    protected open fun bodyBlock(): Block = headBlock()

    /** Whether [block] is this plant's head or body. */
    protected open fun isPlant(block: Block): Boolean =
        block.key() == headBlock().key() || block.key() == bodyBlock().key()

    /** Whether the plant may attach to the block at its base. */
    protected open fun canAttachTo(block: Block): Boolean = true

    /** Whether the plant must be placed in water (kelp, coral). */
    protected open fun requiresWater(): Boolean = false

    /** The head state to place into a free position. */
    protected open fun headState(event: BlockPlacementEvent): Block = event.block

    init {
        onPlace {
            val ahead = instance.getBlockOrAir(blockPosition.relative(growthDirection.blockFace))
            val state = if (isPlant(ahead)) bodyBlock() else headState(this)
            if (requiresWater() && !Fluids.isWater(instance, blockPosition)) {
                isCancelled = true
                return@onPlace
            }
            if (!canSurvive(state, instance, blockPosition)) {
                isCancelled = true
                return@onPlace
            }
            block = state
        }

        onUpdate {
            if (!canSurvive(block, instance, blockPosition)) {
                destroy()
                return@onUpdate
            }
            val ahead = instance.getBlockOrAir(blockPosition.relative(growthDirection.blockFace))
            if (isPlant(ahead)) {
                resultBlock = bodyBlock()
            }
        }
    }

    /** Whether the plant can stay attached at [position]. */
    protected open fun canSurvive(state: Block, instance: Instance, position: BlockVec): Boolean {
        val attachedPosition = position.relative(growthDirection.opposite().blockFace)
        val attached = instance.getBlockOrAir(attachedPosition)
        return canAttachTo(attached) &&
            (isPlant(attached) || Support.isFaceSturdy(instance, attachedPosition, growthDirection.blockFace))
    }
}
