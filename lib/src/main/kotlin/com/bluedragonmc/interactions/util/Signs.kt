package com.bluedragonmc.interactions.util

import net.kyori.adventure.key.Key
import net.minestom.server.instance.block.Block

/** Helpers for sign-family blocks. */
object Signs {
    /** Whether [block] is any hanging sign (standing or wall). */
    fun isHangingSign(block: Block): Boolean = block.key().asString().contains("hanging_sign")

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

