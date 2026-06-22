package com.bluedragonmc.interactions.util

import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.utils.Direction

fun Vec.closestHorizontalFace(): Direction = Direction.HORIZONTAL.maxBy { it.vec().dot(this) }
fun Vec.closestDirections(): Iterable<Direction> = Direction.entries.sortedByDescending { it.vec().dot(this) }

val Direction.blockFace: BlockFace
    get() = BlockFace.fromDirection(this)
