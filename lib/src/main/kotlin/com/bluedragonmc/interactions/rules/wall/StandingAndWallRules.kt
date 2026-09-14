package com.bluedragonmc.interactions.rules.wall

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.event.BlockPlacementEvent
import com.bluedragonmc.interactions.rules.base.StandingAndWallPlacementRule
import com.bluedragonmc.interactions.util.Signs
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.Support
import com.bluedragonmc.interactions.util.axisName
import com.bluedragonmc.interactions.util.blockFace
import com.bluedragonmc.interactions.util.clockWise
import com.bluedragonmc.interactions.util.counterClockWise
import com.bluedragonmc.interactions.util.directionOf
import com.bluedragonmc.interactions.util.getBlockOrAir
import com.bluedragonmc.interactions.util.horizontalDirection
import com.bluedragonmc.interactions.util.rotationDirection
import com.bluedragonmc.interactions.util.rotationSegment
import net.kyori.adventure.key.Key
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.utils.Direction

// --- Signs -------------------------------------------------------------------------------------

/** Signs: standing when placed on top, wall-mounted when clicked against a solid block. */
class SignRule : StandingAndWallPlacementRule(
    BlockSet.fromTag("minecraft:standing_signs"),
    BlockSet.fromTag("minecraft:wall_signs"),
    Direction.DOWN,
    Signs::wallVariant,
) {
    override fun standingState(standing: Block, event: BlockPlacementEvent): Block =
        standing.withProperty(States.ROTATION, rotationSegment(event.player.position.yaw + 180f).toString())

    override fun canSurviveStanding(state: Block, instance: Instance, position: BlockVec): Boolean =
        Support.canSupportCenter(instance, position.relative(BlockFace.BOTTOM), BlockFace.TOP)

    override fun canSurviveWall(state: Block, supportDirection: Direction, instance: Instance, position: BlockVec): Boolean =
        Support.isSolid(instance, position.relative(supportDirection.blockFace))
}

/** Hanging signs: ceiling-mounted when placed from above, otherwise side-attached. */
class HangingSignRule : StandingAndWallPlacementRule(
    BlockSet.matching { it.contains("hanging_sign") && !it.contains("wall_hanging_sign") },
    BlockSet.fromTag("minecraft:wall_hanging_signs"),
    Direction.UP,
    Signs::wallVariant,
) {
    override fun standingState(standing: Block, event: BlockPlacementEvent): Block {
        val abovePosition = event.blockPosition.relative(BlockFace.TOP)
        val above = event.instance.getBlockOrAir(abovePosition)
        val direction = event.player.position.horizontalDirection().toDirection()
        var attachedToMiddle = !Support.isFaceFull(above, BlockFace.BOTTOM) || event.player.isSneaking

        if (Signs.isHangingSign(above) && !event.player.isSneaking) {
            val aboveDirection = above.getProperty(States.FACING)?.let(::directionOf)
                ?: above.getProperty(States.ROTATION)?.toIntOrNull()?.let(::rotationDirection)
            if (aboveDirection != null && aboveDirection.axisName == direction.axisName) attachedToMiddle = false
        }

        val segment = if (attachedToMiddle) {
            rotationSegment(event.player.position.yaw + 180f)
        } else {
            rotationSegment(direction.opposite())
        }
        return standing
            .withProperty(States.ATTACHED, attachedToMiddle.toString())
            .withProperty(States.ROTATION, segment.toString())
    }

    override fun canSurviveStanding(state: Block, instance: Instance, position: BlockVec): Boolean {
        val above = position.relative(BlockFace.TOP)
        return Support.canSupportCenter(instance, above, BlockFace.BOTTOM) ||
            Signs.isHangingSign(instance.getBlockOrAir(above))
    }

    override fun canSurviveWall(state: Block, supportDirection: Direction, instance: Instance, position: BlockVec): Boolean {
        val facing = directionOf(state.getProperty(States.FACING)!!)
        val clockwise = facing.clockWise()
        val counterClockwise = facing.counterClockWise()
        return canAttachTo(state, instance, position.relative(clockwise.blockFace), counterClockwise) ||
            canAttachTo(state, instance, position.relative(counterClockwise.blockFace), clockwise)
    }

    private fun canAttachTo(state: Block, instance: Instance, attachPosition: BlockVec, attachFace: Direction): Boolean {
        val attach = instance.getBlockOrAir(attachPosition)
        val attachFacing = attach.getProperty(States.FACING)?.let(::directionOf)
        if (attach.key() in keys && attachFacing != null) {
            return attachFacing.axisName == directionOf(state.getProperty(States.FACING)!!).axisName
        }
        return Support.isFaceSturdy(instance, attachPosition, attachFace.blockFace)
    }
}

// --- Banners and skulls ------------------------------------------------------------------------

/** Banners: standing on top, wall-mounted against a solid block. */
class BannerRule : StandingAndWallPlacementRule(
    BlockSet.matching { it.endsWith("_banner") && !it.endsWith("_wall_banner") },
    BlockSet.matching { it.endsWith("_wall_banner") },
    Direction.DOWN,
    ::wallBannerVariant,
) {
    override fun standingState(standing: Block, event: BlockPlacementEvent): Block =
        standing.withProperty(States.ROTATION, rotationSegment(event.player.position.yaw + 180f).toString())

    override fun canSurviveStanding(state: Block, instance: Instance, position: BlockVec): Boolean =
        Support.canSupportCenter(instance, position.relative(BlockFace.BOTTOM), BlockFace.TOP)

    override fun canSurviveWall(state: Block, supportDirection: Direction, instance: Instance, position: BlockVec): Boolean =
        Support.isSolid(instance, position.relative(supportDirection.blockFace))
}

/** Skulls and heads: standing when placed on top, wall-mounted otherwise. */
class SkullRule : StandingAndWallPlacementRule(
    BlockSet.matching { (it.endsWith("_head") || it.endsWith("_skull")) && !it.contains("_wall_") },
    BlockSet.matching { (it.endsWith("_head") || it.endsWith("_skull")) && it.contains("_wall_") },
    Direction.DOWN,
    ::wallSkullVariant,
) {
    override fun standingState(standing: Block, event: BlockPlacementEvent): Block =
        standing.withProperty(States.ROTATION, rotationSegment(event.player.position.yaw).toString())

    override fun canSurviveWall(state: Block, supportDirection: Direction, instance: Instance, position: BlockVec): Boolean =
        !instance.getBlockOrAir(position.relative(supportDirection.blockFace)).isAir()
}

// --- Torches -----------------------------------------------------------------------------------

/** Torches: standing on top, wall-mounted against a block. */
class TorchRule : StandingAndWallPlacementRule(
    BlockSet.from(Block.TORCH, Block.SOUL_TORCH, Block.REDSTONE_TORCH, Block.COPPER_TORCH),
    BlockSet.from(Block.WALL_TORCH, Block.SOUL_WALL_TORCH, Block.REDSTONE_WALL_TORCH, Block.COPPER_WALL_TORCH),
    Direction.DOWN,
    ::wallTorchVariant,
) {
    override fun canSurviveStanding(state: Block, instance: Instance, position: BlockVec): Boolean =
        Support.canSupportCenter(instance, position.relative(BlockFace.BOTTOM), BlockFace.TOP)
}

private fun wallBannerVariant(block: Block): Block? {
    val path = block.key().value()
    if (path.endsWith("_wall_banner")) return block
    if (!path.endsWith("_banner")) return null
    return Block.fromKey(Key.key(block.key().namespace(), path.removeSuffix("_banner") + "_wall_banner").asString())
}

private fun wallSkullVariant(block: Block): Block? {
    val path = block.key().value()
    if (path.contains("_wall_")) return block
    val wallPath = when {
        path.endsWith("_skull") -> path.removeSuffix("_skull") + "_wall_skull"
        path.endsWith("_head") -> path.removeSuffix("_head") + "_wall_head"
        else -> return null
    }
    return Block.fromKey(Key.key(block.key().namespace(), wallPath).asString())
}

private fun wallTorchVariant(block: Block): Block? {
    val path = block.key().value()
    if (path.endsWith("_wall_torch")) return block
    val wallPath = when {
        path == "torch" -> "wall_torch"
        path.endsWith("_torch") -> path.removeSuffix("_torch") + "_wall_torch"
        else -> return null
    }
    return Block.fromKey(Key.key(block.key().namespace(), wallPath).asString())
}
