package com.bluedragonmc.interactions.util

import net.minestom.server.coordinate.Point
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block

/**
 * Shared helper for the `waterlogged` block-state property.
 *
 * Rules should call [apply] after computing their placement state so the property is derived from
 * the fluid at the new position. The call is a no-op for blocks without a `waterlogged` property.
 */
object Waterlogged {

    /** Sets `waterlogged` from the fluid at [position], if the block has the property. */
    fun apply(block: Block, instance: Instance, position: Point): Block =
        apply(block, Fluids.isWater(instance, position))

    /** Sets `waterlogged` to [value], if the block has the property. */
    fun apply(block: Block, value: Boolean): Block {
        if (block.getProperty(States.WATERLOGGED) == null) return block
        return block.withProperty(States.WATERLOGGED, value.toString())
    }
}
