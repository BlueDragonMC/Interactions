package com.bluedragonmc.interactions.util

import net.kyori.adventure.key.Key
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Helpers for sign-family blocks. */
object Signs {
    /** Whether [block] is any hanging sign (standing or wall). */
    fun isHangingSign(block: Block): Boolean = block.key().asString().contains("hanging_sign")

    /**
     * Whether the player clicked the front text of [block].
     *
     * Wall signs use their `facing`; standing signs derive the front from their `rotation` segment
     * and the player's position relative to the sign.
     */
    fun isFacingFrontText(block: Block, player: Player, position: Point, clickedFace: BlockFace): Boolean {
        val facingName = block.getProperty(States.FACING)
        if (facingName != null) {
            return clickedFace.name.equals(facingName, ignoreCase = true)
        }

        val rotation = block.getProperty(States.ROTATION)?.toIntOrNull() ?: return true
        val frontYawRadians = rotation * (PI / 8.0)
        val frontX = -sin(frontYawRadians)
        val frontZ = cos(frontYawRadians)
        val deltaX = player.position.x() - (position.blockX() + 0.5)
        val deltaZ = player.position.z() - (position.blockZ() + 0.5)
        return deltaX * frontX + deltaZ * frontZ > 0.0
    }

    /** Maps a standing sign to its wall variant, or returns [block] if it is already a wall sign. */
    fun wallVariant(block: Block): Block? {
        val path = block.key().value()
        val wallPath = when {
            path.endsWith("_wall_sign") || path.endsWith("_wall_hanging_sign") -> return block
            path.endsWith("_hanging_sign") -> path.removeSuffix("_hanging_sign") + "_wall_hanging_sign"
            path.endsWith("_sign") -> path.removeSuffix("_sign") + "_wall_sign"
            else -> return null
        }
        return Block.fromKey(Key.key(block.key().namespace(), wallPath).asString())
    }
}

