package com.bluedragonmc.interactions.util

import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.utils.Direction
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

fun Vec.closestHorizontalFace(): Direction = Direction.HORIZONTAL.maxBy { it.vec().dot(this) }
fun Vec.closestDirections(): Iterable<Direction> = Direction.entries.sortedByDescending { it.vec().dot(this) }

val Direction.blockFace: BlockFace
    get() = BlockFace.fromDirection(this)

/** Directions ordered by how closely they match the position's yaw and pitch. */
fun Pos.orderedDirections(): List<Direction> {
    val pitch = Math.toRadians(pitch.toDouble())
    val yaw = -Math.toRadians(yaw.toDouble())
    val pitchSin = sin(pitch)
    val pitchCos = cos(pitch)
    val yawSin = sin(yaw)
    val yawCos = cos(yaw)
    val xPos = yawSin > 0.0
    val yPos = pitchSin < 0.0
    val zPos = yawCos > 0.0
    val xYaw = if (xPos) yawSin else -yawSin
    val yMag = if (yPos) -pitchSin else pitchSin
    val zYaw = if (zPos) yawCos else -yawCos
    val xMag = xYaw * pitchCos
    val zMag = zYaw * pitchCos
    val axisX = if (xPos) Direction.EAST else Direction.WEST
    val axisY = if (yPos) Direction.UP else Direction.DOWN
    val axisZ = if (zPos) Direction.SOUTH else Direction.NORTH
    return when {
        xYaw > zYaw -> when {
            yMag > xMag -> makeDirectionArray(axisY, axisX, axisZ)
            zMag > yMag -> makeDirectionArray(axisX, axisZ, axisY)
            else -> makeDirectionArray(axisX, axisY, axisZ)
        }
        yMag > zMag -> makeDirectionArray(axisY, axisZ, axisX)
        xMag > yMag -> makeDirectionArray(axisZ, axisX, axisY)
        else -> makeDirectionArray(axisZ, axisY, axisX)
    }
}

private fun makeDirectionArray(
    axis1: Direction,
    axis2: Direction,
    axis3: Direction,
): List<Direction> = listOf(axis1, axis2, axis3, axis3.opposite(), axis2.opposite(), axis1.opposite())

/** The horizontal direction the player is facing. */
fun Pos.horizontalDirection(): BlockFace {
    val index = floor(yaw / 90.0 + 0.5).toInt() and 3
    return HORIZONTAL_FACES[index]
}

/** The direction the player is looking closest to. */
fun Pos.nearestLookingDirection(): Direction = orderedDirections().first()

/** The vertical direction (`up`/`down`) the player is looking closest to. */
fun Pos.nearestLookingVerticalDirection(): Direction = if (pitch < 0f) Direction.UP else Direction.DOWN

private val HORIZONTAL_FACES = arrayOf(BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST)

/** The horizontal direction 90 degrees clockwise from this one. */
fun Direction.clockWise(): Direction = when (this) {
    Direction.NORTH -> Direction.EAST
    Direction.SOUTH -> Direction.WEST
    Direction.WEST -> Direction.NORTH
    Direction.EAST -> Direction.SOUTH
    else -> this
}

/** The horizontal direction 90 degrees counter-clockwise from this one. */
fun Direction.counterClockWise(): Direction = when (this) {
    Direction.NORTH -> Direction.WEST
    Direction.SOUTH -> Direction.EAST
    Direction.WEST -> Direction.SOUTH
    Direction.EAST -> Direction.NORTH
    else -> this
}

/** The axis name (`x`, `y` or `z`) this direction is aligned to. */
val Direction.axisName: String
    get() = when (this) {
        Direction.UP, Direction.DOWN -> "y"
        Direction.NORTH, Direction.SOUTH -> "z"
        Direction.WEST, Direction.EAST -> "x"
    }

/** The axis name (`x`, `y` or `z`) this face is aligned to. */
val BlockFace.axisName: String
    get() = this.toDirection().axisName

/** Parses a lowercase `Direction` property value (e.g. `"north"`). */
fun directionOf(value: String): Direction = Direction.valueOf(value.uppercase())
