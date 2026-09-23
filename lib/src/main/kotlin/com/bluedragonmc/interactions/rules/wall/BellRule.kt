package com.bluedragonmc.interactions.rules.wall

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.Support
import com.bluedragonmc.interactions.util.getBlockOrAir
import com.bluedragonmc.interactions.util.horizontalDirection
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace

/** Bells mount on the floor, ceiling (centered) or a wall (single or double). */
class BellRule : PlacementRule(BlockSet.from(Block.BELL)) {
    init {
        onPlace {
            val clickedFace = blockFace
            val horizontalFacing = player.position.horizontalDirection()

            if (clickedFace == BlockFace.TOP || clickedFace == BlockFace.BOTTOM) {
                val attachment = if (clickedFace == BlockFace.BOTTOM) "ceiling" else "floor"
                val supportDirection = clickedFace.oppositeFace
                if (!canSurvive(instance, blockPosition.relative(supportDirection), supportDirection, attachment)) {
                    isCancelled = true
                    return@onPlace
                }
                block = block
                    .withProperty(States.ATTACHMENT, attachment)
                    .withProperty(States.FACING, horizontalFacing.name.lowercase())
                return@onPlace
            }

            val facing = clickedFace.oppositeFace
            val attachment = if (isDoubleAttached(instance, blockPosition, clickedFace)) "double_wall" else "single_wall"
            val result = block
                .withProperty(States.ATTACHMENT, attachment)
                .withProperty(States.FACING, facing.name.lowercase())

            if (canSurvive(instance, blockPosition.relative(facing), facing, attachment)) {
                block = result
                return@onPlace
            }

            val fallback = if (Support.canSupportRigidBlock(instance, blockPosition.relative(BlockFace.BOTTOM))) {
                "floor"
            } else {
                "ceiling"
            }
            val fallbackDirection = if (fallback == "floor") BlockFace.BOTTOM else BlockFace.TOP
            if (!canSurvive(instance, blockPosition.relative(fallbackDirection), fallbackDirection, fallback)) {
                isCancelled = true
                return@onPlace
            }
            block = result.withProperty(States.ATTACHMENT, fallback)
        }

        onUpdate {
            val attachment = block.getProperty(States.ATTACHMENT)
            val supportDirection = when (attachment) {
                "floor" -> BlockFace.BOTTOM
                "ceiling" -> BlockFace.TOP
                "single_wall", "double_wall" -> block.getProperty(States.FACING)?.let(::faceOf)
                else -> null
            } ?: return@onUpdate

            if (fromFace == supportDirection && attachment != "double_wall" &&
                !canSurvive(instance, blockPosition.relative(supportDirection), supportDirection, attachment)
            ) {
                destroy()
                return@onUpdate
            }

            val facing = block.getProperty(States.FACING)?.let(::faceOf)
            if (facing != null && fromFace.toDirection().horizontal() && facing == fromFace) {
                if (attachment == "double_wall" &&
                    !Support.canSupportRigidBlock(instance, blockPosition.relative(fromFace), fromFace.oppositeFace)
                ) {
                    resultBlock = block
                        .withProperty(States.ATTACHMENT, "single_wall")
                        .withProperty(States.FACING, fromFace.oppositeFace.name.lowercase())
                    return@onUpdate
                }
                if (attachment == "single_wall" && supportDirection.oppositeFace == fromFace &&
                    Support.canSupportRigidBlock(instance, blockPosition.relative(fromFace), facing)
                ) {
                    resultBlock = block.withProperty(States.ATTACHMENT, "double_wall")
                }
            }
        }
    }

    private fun isDoubleAttached(instance: Instance, position: BlockVec, clickedFace: BlockFace): Boolean =
        when (clickedFace) {
            BlockFace.WEST, BlockFace.EAST ->
                Support.canSupportRigidBlock(instance, position.relative(BlockFace.WEST), BlockFace.EAST) &&
                    Support.canSupportRigidBlock(instance, position.relative(BlockFace.EAST), BlockFace.WEST)
            BlockFace.NORTH, BlockFace.SOUTH ->
                Support.canSupportRigidBlock(instance, position.relative(BlockFace.NORTH), BlockFace.SOUTH) &&
                    Support.canSupportRigidBlock(instance, position.relative(BlockFace.SOUTH), BlockFace.NORTH)
            else -> false
        }

    private fun canSurvive(instance: Instance, supportPosition: BlockVec, supportDirection: BlockFace, attachment: String?): Boolean {
        val supportFace = supportDirection.oppositeFace
        return if (attachment == "ceiling") {
            Support.canSupportCenter(instance, supportPosition, supportFace)
        } else {
            Support.canSupportRigidBlock(instance, supportPosition, supportFace)
        }
    }
}

private fun faceOf(name: String): BlockFace? = when (name) {
    "north" -> BlockFace.NORTH
    "east" -> BlockFace.EAST
    "south" -> BlockFace.SOUTH
    "west" -> BlockFace.WEST
    else -> null
}
