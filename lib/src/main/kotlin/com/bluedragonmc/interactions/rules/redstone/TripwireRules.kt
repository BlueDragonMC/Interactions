package com.bluedragonmc.interactions.rules.redstone

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.Support
import com.bluedragonmc.interactions.util.blockFace
import com.bluedragonmc.interactions.util.getBlockOrAir
import com.bluedragonmc.interactions.util.orderedDirections
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Point
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace

/** Tripwire connects to adjacent tripwire and to hooks that face it. */
class TripwireRule : PlacementRule(BlockSet.from(Block.TRIPWIRE)) {
    init {
        onPlace {
            block = block
                .withProperty(States.NORTH, connects(instance, blockPosition, BlockFace.NORTH, block).toString())
                .withProperty(States.EAST, connects(instance, blockPosition, BlockFace.EAST, block).toString())
                .withProperty(States.SOUTH, connects(instance, blockPosition, BlockFace.SOUTH, block).toString())
                .withProperty(States.WEST, connects(instance, blockPosition, BlockFace.WEST, block).toString())
        }

        onUpdate {
            if (fromFace == BlockFace.TOP || fromFace == BlockFace.BOTTOM) return@onUpdate
            resultBlock = block.withProperty(
                States.connection(fromFace),
                connects(instance, blockPosition, fromFace, block).toString(),
            )
        }
    }
}

/** Tripwire hooks mount on a horizontal support and report whether a complete wire line is attached. */
class TripwireHookRule : PlacementRule(BlockSet.from(Block.TRIPWIRE_HOOK)) {
    init {
        onPlace {
            var facing: BlockFace? = null
            for (direction in player.position.orderedDirections()) {
                if (direction.vertical()) continue
                val face = direction.blockFace
                if (Support.canSupportCenter(instance, blockPosition.relative(face), face.oppositeFace)) {
                    facing = face.oppositeFace
                    break
                }
            }

            if (facing == null) {
                isCancelled = true
                return@onPlace
            }

            block = withLineState(
                block
                    .withProperty(States.FACING, facing.name.lowercase())
                    .withProperty(States.POWERED, "false")
                    .withProperty(States.ATTACHED, "false"),
                instance,
                blockPosition,
                facing,
            )
        }

        onUpdate {
            val facing = block.getProperty(States.FACING)?.let(::faceOf) ?: return@onUpdate
            if (!Support.canSupportCenter(instance, blockPosition.relative(facing.oppositeFace), facing)) {
                destroy()
                return@onUpdate
            }
            if (!fromFace.toDirection().horizontal()) return@onUpdate
            resultBlock = withLineState(block, instance, blockPosition, facing)
        }
    }
}

private fun connects(instance: Instance, position: BlockVec, face: BlockFace, current: Block): Boolean {
    val neighbor = instance.getBlockOrAir(position.relative(face))
    if (neighbor.key() == Block.TRIPWIRE_HOOK.key()) {
        return neighbor.getProperty(States.FACING) == face.oppositeFace.name.lowercase()
    }
    return neighbor.key() == current.key()
}

private fun withLineState(base: Block, instance: Instance, position: BlockVec, facing: BlockFace): Block {
    var linePosition: Point = position.relative(facing)
    var hasWire = false
    var powered = false

    repeat(MAX_WIRE_LENGTH) {
        val lineBlock = instance.getBlockOrAir(linePosition)
        if (lineBlock.key() == Block.TRIPWIRE.key()) {
            hasWire = true
            powered = powered || lineBlock.getProperty(States.POWERED) == "true"
            linePosition = linePosition.relative(facing)
            return@repeat
        }

        val oppositeHook = lineBlock.key() == Block.TRIPWIRE_HOOK.key() &&
            lineBlock.getProperty(States.FACING) == facing.oppositeFace.name.lowercase()
        return base
            .withProperty(States.ATTACHED, (hasWire && oppositeHook).toString())
            .withProperty(States.POWERED, (hasWire && oppositeHook && powered).toString())
    }

    return base.withProperty(States.ATTACHED, "false").withProperty(States.POWERED, "false")
}

private fun faceOf(name: String): BlockFace? = when (name) {
    "north" -> BlockFace.NORTH
    "east" -> BlockFace.EAST
    "south" -> BlockFace.SOUTH
    "west" -> BlockFace.WEST
    else -> null
}

private const val MAX_WIRE_LENGTH = 42
