package com.bluedragonmc.interactions.rules.misc

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.util.Signals
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.Support
import com.bluedragonmc.interactions.util.Tags
import com.bluedragonmc.interactions.util.axisName
import com.bluedragonmc.interactions.util.blockFace
import com.bluedragonmc.interactions.util.environmentBoolean
import com.bluedragonmc.interactions.util.getBlockOrAir
import com.bluedragonmc.interactions.util.horizontalDirection
import com.bluedragonmc.interactions.util.nearestLookingDirection
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.utils.Direction

/** Huge mushroom blocks and stems track which faces connect to an identical block. */
class HugeMushroomRule : PlacementRule(
    BlockSet.from(Block.RED_MUSHROOM_BLOCK, Block.BROWN_MUSHROOM_BLOCK, Block.MUSHROOM_STEM),
) {
    init {
        onPlace { block = neighborProperties(instance, blockPosition, block) }
        onUpdate { resultBlock = neighborProperties(instance, blockPosition, block) }
    }

    private fun neighborProperties(instance: Instance, position: BlockVec, base: Block): Block {
        var result = base
        for (face in BlockFace.entries) {
            val neighbor = instance.getBlockOrAir(position.relative(face))
            result = result.withProperty(States.connection(face), (neighbor.key() != base.key()).toString())
        }
        return result
    }
}

/** Creaking hearts derive their state from the pale oak logs around them and the dimension attribute. */
class CreakingHeartRule : PlacementRule(BlockSet.from(Block.CREAKING_HEART)) {
    init {
        onPlace {
            val placed = block.withProperty(States.AXIS, blockFace.axisName)
            block = updateHeart(instance, blockPosition, placed)
        }
        onUpdate { resultBlock = updateHeart(instance, blockPosition, block) }
    }

    private fun updateHeart(instance: Instance, position: BlockVec, block: Block): Block {
        if (block.getProperty(States.CREAKING_HEART_STATE) != "uprooted" || !hasRequiredLogs(instance, position, block)) {
            return block
        }
        val active = instance.environmentBoolean("minecraft:creaking_active", false)
        return block.withProperty(States.CREAKING_HEART_STATE, if (active) "awake" else "dormant")
    }

    private fun hasRequiredLogs(instance: Instance, position: BlockVec, block: Block): Boolean {
        val axis = block.getProperty(States.AXIS)
        val faces = when (axis) {
            "x" -> listOf(BlockFace.WEST, BlockFace.EAST)
            "z" -> listOf(BlockFace.NORTH, BlockFace.SOUTH)
            else -> listOf(BlockFace.BOTTOM, BlockFace.TOP)
        }
        return faces.all { face ->
            val neighbor = instance.getBlockOrAir(position.relative(face))
            neighbor.key() in Tags.PALE_OAK_LOGS && neighbor.getProperty(States.AXIS) == axis
        }
    }
}

/** Potent sulfur erupts when water sits above a geyser-causing block. */
class PotentSulfurRule : PlacementRule(BlockSet.from(Block.POTENT_SULFUR)) {
    init {
        onPlace { block = validState(instance, blockPosition, block) }
        onUpdate { resultBlock = validState(instance, blockPosition, block) }
    }

    private fun validState(instance: Instance, position: BlockVec, block: Block): Block {
        val above = instance.getBlockOrAir(position.relative(BlockFace.TOP))
        if (!isSourceWater(above)) return block.withProperty(States.POTENT_SULFUR_STATE, "dry")

        val below = instance.getBlockOrAir(position.relative(BlockFace.BOTTOM))
        if (below.key() in Tags.CONTINUOUS_GEYSER && isSourceFluid(below)) {
            return block.withProperty(States.POTENT_SULFUR_STATE, "continuous")
        }
        if (below.key() in Tags.PERIODIC_GEYSER && isSourceFluid(below)) {
            return if (block.getProperty(States.POTENT_SULFUR_STATE) == "erupting") {
                block
            } else {
                block.withProperty(States.POTENT_SULFUR_STATE, "dormant")
            }
        }
        return block.withProperty(States.POTENT_SULFUR_STATE, "wet")
    }

    private fun isSourceFluid(block: Block): Boolean = !block.fluid() || block.getProperty(States.LEVEL) == "0"

    private fun isSourceWater(block: Block): Boolean =
        block.key() == Block.WATER.key() && block.getProperty(States.LEVEL) == "0"
}

/** Crafters face away from the player's nearest look and track neighbor signals. */
class CrafterRule : PlacementRule(BlockSet.from(Block.CRAFTER)) {
    init {
        onPlace {
            val nearestLooking = player.position.nearestLookingDirection().blockFace.oppositeFace
            val horizontal = player.position.horizontalDirection()
            val verticalDirection = when (nearestLooking) {
                BlockFace.BOTTOM -> horizontal.oppositeFace
                BlockFace.TOP -> horizontal
                else -> BlockFace.TOP
            }
            val triggered = Signals.hasNeighborSignal(instance, blockPosition)
            block = block
                .withProperty(States.ORIENTATION, "${orientationName(nearestLooking)}_${orientationName(verticalDirection)}")
                .withProperty(States.TRIGGERED, triggered.toString())
        }

        onUpdate {
            val triggered = Signals.hasNeighborSignal(instance, blockPosition)
            resultBlock = if (triggered) {
                block.withProperty(States.TRIGGERED, "true")
            } else {
                block.withProperty(States.TRIGGERED, "false").withProperty(States.CRAFTING, "false")
            }
        }
    }
}

/** Jigsaw blocks face the clicked direction, with a top oriented by the player. */
class JigsawRule : PlacementRule(BlockSet.from(Block.JIGSAW)) {
    init {
        onPlace {
            val front = blockFace
            val top = if (front.toDirection().vertical()) {
                player.position.horizontalDirection().oppositeFace
            } else {
                BlockFace.TOP
            }
            block = block.withProperty(States.ORIENTATION, "${orientationName(front)}_${orientationName(top)}")
        }
    }
}

/** Pale moss carpets sit under a sturdy block or extend sideways into neighbouring blocks. */
class PaleMossCarpetRule : PlacementRule(BlockSet.from(Block.PALE_MOSS_CARPET)) {
    init {
        onPlace {
            val hasBase = isSturdy(instance, blockPosition.relative(BlockFace.BOTTOM))
            val sides = sides(instance, blockPosition)
            if (!hasBase && sides.all { it == "none" }) {
                isCancelled = true
                return@onPlace
            }
            block = withSides(block.withProperty(States.BASE, hasBase.toString()), sides)
        }

        onUpdate {
            if (fromFace != BlockFace.BOTTOM && !fromFace.toDirection().horizontal()) return@onUpdate
            val hasBase = isSturdy(instance, blockPosition.relative(BlockFace.BOTTOM))
            val sides = sides(instance, blockPosition)
            if (!hasBase && sides.all { it == "none" }) {
                destroy()
                return@onUpdate
            }
            resultBlock = withSides(block.withProperty(States.BASE, hasBase.toString()), sides)
        }
    }

    private fun withSides(block: Block, sides: List<String>): Block = block
        .withProperty(States.NORTH, sides[0])
        .withProperty(States.EAST, sides[1])
        .withProperty(States.SOUTH, sides[2])
        .withProperty(States.WEST, sides[3])

    private fun sides(instance: Instance, position: BlockVec): List<String> = listOf(
        BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST,
    ).map { face ->
        val neighbor = instance.getBlockOrAir(position.relative(face))
        if (Support.canSupportCenter(instance, position.relative(face), face.oppositeFace) && !neighbor.air()) "low" else "none"
    }

    private fun isSturdy(instance: Instance, position: BlockVec): Boolean =
        Support.canSupportCenter(instance, position, BlockFace.TOP)
}

/** Pale hanging moss keeps a `tip` flag and drops when it loses its attachment. */
class PaleHangingMossRule : PlacementRule(BlockSet.from(Block.PALE_HANGING_MOSS)) {
    init {
        onPlace {
            val below = instance.getBlockOrAir(blockPosition.relative(BlockFace.BOTTOM))
            block = block.withProperty(States.TIP, (below.key() != block.key()).toString())
        }

        onUpdate {
            if (!canStay(instance, blockPosition)) {
                destroy()
                return@onUpdate
            }
            val below = instance.getBlockOrAir(blockPosition.relative(BlockFace.BOTTOM))
            resultBlock = block.withProperty(States.TIP, (below.key() != block.key()).toString())
        }
    }

    private fun canStay(instance: Instance, position: BlockVec): Boolean {
        val above = instance.getBlockOrAir(position.relative(BlockFace.TOP))
        return above.key() == Block.PALE_HANGING_MOSS.key() || Support.isFaceFull(above, BlockFace.BOTTOM)
    }
}

private fun orientationName(face: BlockFace): String = when (face) {
    BlockFace.TOP -> "up"
    BlockFace.BOTTOM -> "down"
    else -> face.name.lowercase()
}
