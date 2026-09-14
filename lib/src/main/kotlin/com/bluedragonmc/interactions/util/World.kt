package com.bluedragonmc.interactions.util

import net.minestom.server.coordinate.Point
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block

/**
 * Returns the block at [position], or [Block.AIR] when the position's chunk is not loaded.
 *
 * Rules frequently probe neighbor positions that may lie outside a loaded chunk; this avoids the
 * exception Minestom throws for unloaded chunks.
 */
fun Instance.getBlockOrAir(position: Point): Block =
    if (isChunkLoaded(position)) getBlock(position) else Block.AIR
