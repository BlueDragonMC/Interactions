package com.bluedragonmc.interactions.util

import net.minestom.server.coordinate.Point
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.BlockFace

/**
 * Neighbor redstone signal queries.
 */
object Signals {
    const val MAX_POWER = 15

    /** Whether any neighbor sends a redstone signal into [position]. */
    fun hasNeighborSignal(instance: Instance, position: Point): Boolean =
        BlockFace.entries.any { signal(instance, position.relative(it)) > 0 }

    /** The direct signal reported by the block at [neighbor]. */
    fun signal(instance: Instance, neighbor: Point): Int {
        val block = instance.getBlockOrAir(neighbor)
        if (block.getProperty(States.POWERED) == "true") return MAX_POWER
        return block.getProperty(States.POWER)?.toIntOrNull() ?: 0
    }
}
