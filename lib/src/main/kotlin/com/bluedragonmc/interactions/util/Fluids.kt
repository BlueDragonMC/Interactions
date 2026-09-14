package com.bluedragonmc.interactions.util

import net.kyori.adventure.key.Key
import net.minestom.server.coordinate.Point
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block

/**
 * Fluid checks used to derive the `waterlogged` property and to validate fluid-supported plants.
 */
object Fluids {
    private val WATER = Key.key("minecraft:water")

    /** Whether the block at [position] is a water block (source or flowing). */
    fun isWater(instance: Instance, position: Point): Boolean = isWater(instance.getBlockOrAir(position))

    fun isWater(block: Block): Boolean = block.key() == WATER
}
