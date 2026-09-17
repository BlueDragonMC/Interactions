package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.util.*
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.utils.Direction

/**
 * Base for speleothems (pointed dripstone, sulfur spikes): a `vertical_direction` and a `thickness`
 * (`tip`/`tip_merge`/`frustum`/`middle`/`base`) derived from neighboring speleothems.
 *
 * A speleothem survives while the block behind its tip (above a stalactite, below a stalagmite) is
 * sturdy or is another speleothem of the same type pointing the same way; otherwise it is removed.
 */
abstract class SpeleothemPlacementRule(blocks: BlockSet) : PlacementRule(blocks) {

    init {
        onPlace {
            val defaultDirection = player.position.nearestLookingVerticalDirection().opposite()
            val tipDirection = calculateTipDirection(instance, blockPosition, defaultDirection)
            if (tipDirection == null) {
                isCancelled = true
                return@onPlace
            }
            val thickness = thickness(instance, blockPosition, tipDirection, mergeOpposingTips = !context.isSneaking)
            block = Waterlogged.apply(
                block
                    .withProperty(States.VERTICAL_DIRECTION, tipDirection.name.lowercase())
                    .withProperty(States.THICKNESS, thickness),
                instance,
                blockPosition,
            )
        }

        onUpdate {
            if (!fromFace.toDirection().vertical()) return@onUpdate
            val tipDirection = directionOf(block.getProperty(States.VERTICAL_DIRECTION)!!)
            if (fromFace == tipDirection.opposite().blockFace && !canSurvive(block, instance, blockPosition)) {
                destroy()
                return@onUpdate
            }
            val mergeOpposingTips = block.getProperty(States.THICKNESS) == "tip_merge"
            resultBlock = block.withProperty(
                States.THICKNESS,
                thickness(instance, blockPosition, tipDirection, mergeOpposingTips),
            )
        }
    }

    /** Whether [state] can stay attached at [position]. */
    private fun canSurvive(state: Block, instance: Instance, position: BlockVec): Boolean =
        isValidSpeleothemPlacement(instance, position, directionOf(state.getProperty(States.VERTICAL_DIRECTION)!!))

    /** Picks the tip direction: prefer the way the player is looking, then the opposite. */
    private fun calculateTipDirection(
        instance: Instance,
        position: BlockVec,
        defaultDirection: Direction,
    ): Direction? {
        if (isValidSpeleothemPlacement(instance, position, defaultDirection)) return defaultDirection
        val opposite = defaultDirection.opposite()
        if (isValidSpeleothemPlacement(instance, position, opposite)) return opposite
        return null
    }

    /** Whether a speleothem pointing [tipDirection] could attach at [position]. */
    private fun isValidSpeleothemPlacement(
        instance: Instance,
        position: BlockVec,
        tipDirection: Direction,
    ): Boolean {
        val behindPosition = position.relative(tipDirection.opposite().blockFace)
        val behind = instance.getBlockOrAir(behindPosition)
        if (Support.isFaceSturdy(instance, behindPosition, tipDirection.blockFace)) return true
        return isSpeleothemWithDirection(behind, tipDirection) && behind.key() in keys
    }

    /** Derives the thickness segment from the speleothem in front along [tipDirection]. */
    private fun thickness(
        instance: Instance,
        position: BlockVec,
        tipDirection: Direction,
        mergeOpposingTips: Boolean,
    ): String {
        val baseDirection = tipDirection.opposite()
        val inFront = instance.getBlockOrAir(position.relative(tipDirection.blockFace))
        if (isSpeleothemWithDirection(inFront, baseDirection) && inFront.key() in keys) {
            val merged = !mergeOpposingTips && inFront.getProperty(States.THICKNESS) != "tip_merge"
            return if (merged) "tip" else "tip_merge"
        }
        if (!isSpeleothemWithDirection(inFront, tipDirection)) return "tip"

        val inFrontThickness = inFront.getProperty(States.THICKNESS)
        if (inFrontThickness != "tip" && inFrontThickness != "tip_merge") {
            val behind = instance.getBlockOrAir(position.relative(baseDirection.blockFace))
            return if (isSpeleothemWithDirection(behind, tipDirection)) "middle" else "base"
        }
        return "frustum"
    }

    private fun isSpeleothemWithDirection(block: Block, direction: Direction): Boolean =
        block.key() in Tags.SPELEOTHEMS &&
            block.getProperty(States.VERTICAL_DIRECTION) == direction.name.lowercase()
}
