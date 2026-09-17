package com.bluedragonmc.interactions.util

import net.kyori.adventure.key.Key
import net.minestom.server.collision.BoundingBox
import net.minestom.server.collision.ShapeImpl
import net.minestom.server.coordinate.Point
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace

/**
 * Face/full-shape support checks.
 */
object Support {

    private const val EPSILON = 1.0E-7

    fun isFaceFull(instance: Instance, position: Point, face: BlockFace): Boolean =
        isFaceFull(instance.getBlockOrAir(position), face)

    fun isFaceFull(block: Block, face: BlockFace): Boolean =
        block.collisionShape().isFaceFull(face)

    /**
     * Whether the block's collision shape is a single full cube.
     *
     * Stricter than [isFaceFull] (a top slab has a full top face but is not a full block).
     */
    fun isFullBlock(block: Block): Boolean {
        val boxes = collisionBoxes(block)
        if (boxes.size != 1) return false
        val box = boxes[0]
        return box.minX() <= EPSILON && box.minY() <= EPSILON && box.minZ() <= EPSILON &&
            box.maxX() >= 1.0 - EPSILON && box.maxY() >= 1.0 - EPSILON && box.maxZ() >= 1.0 - EPSILON
    }

    /**
     * The rectangles [block]'s collision shape projects onto [face], i.e. the parts of the shape that
     * touch that face.
     *
     * The returned rectangles use face-local coordinates: `(x, z)` for [BlockFace.TOP] and
     * [BlockFace.BOTTOM], `(x, y)` for [BlockFace.NORTH] and [BlockFace.SOUTH], and `(y, z)` for
     * [BlockFace.WEST] and [BlockFace.EAST].
     */
    fun faceRectangles(block: Block, face: BlockFace): List<Rect> {
        val rectangles = ArrayList<Rect>()
        for (box in collisionBoxes(block)) {
            val rect = when (face) {
                BlockFace.BOTTOM ->
                    if (box.minY() <= EPSILON) Rect(box.minX(), box.minZ(), box.maxX(), box.maxZ()) else null
                BlockFace.TOP ->
                    if (box.maxY() >= 1.0 - EPSILON) Rect(box.minX(), box.minZ(), box.maxX(), box.maxZ()) else null
                BlockFace.NORTH ->
                    if (box.minZ() <= EPSILON) Rect(box.minX(), box.minY(), box.maxX(), box.maxY()) else null
                BlockFace.SOUTH ->
                    if (box.maxZ() >= 1.0 - EPSILON) Rect(box.minX(), box.minY(), box.maxX(), box.maxY()) else null
                BlockFace.WEST ->
                    if (box.minX() <= EPSILON) Rect(box.minY(), box.minZ(), box.maxY(), box.maxZ()) else null
                BlockFace.EAST ->
                    if (box.maxX() >= 1.0 - EPSILON) Rect(box.minY(), box.minZ(), box.maxY(), box.maxZ()) else null
            }
            if (rect != null) rectangles += rect
        }
        return rectangles
    }

    /** Whether [covering] completely covers [target]. */
    fun covers(covering: List<Rect>, target: Rect): Boolean {
        var remaining = listOf(target)
        for (rect in covering) {
            val next = ArrayList<Rect>()
            for (toCover in remaining) next.addAll(subtract(rect, toCover))
            remaining = next
            if (remaining.isEmpty()) return true
        }
        return remaining.isEmpty()
    }

    /** The parts of [target] that are not covered by [covering]. */
    private fun subtract(covering: Rect, target: Rect): List<Rect> {
        val overlapMinU = maxOf(covering.minU, target.minU)
        val overlapMinV = maxOf(covering.minV, target.minV)
        val overlapMaxU = minOf(covering.maxU, target.maxU)
        val overlapMaxV = minOf(covering.maxV, target.maxV)
        if (overlapMaxU - overlapMinU <= EPSILON || overlapMaxV - overlapMinV <= EPSILON) return listOf(target)
        val remaining = ArrayList<Rect>(4)
        if (target.minV < overlapMinV) remaining += Rect(target.minU, target.minV, target.maxU, overlapMinV)
        if (target.maxV > overlapMaxV) remaining += Rect(target.minU, overlapMaxV, target.maxU, target.maxV)
        if (target.minU < overlapMinU) remaining += Rect(target.minU, overlapMinV, overlapMinU, overlapMaxV)
        if (target.maxU > overlapMaxU) remaining += Rect(overlapMaxU, overlapMinV, target.maxU, overlapMaxV)
        return remaining
    }

    private fun collisionBoxes(block: Block): List<BoundingBox> =
        (block.collisionShape() as? ShapeImpl)?.boundingBoxes() ?: emptyList()

    /** Whether the block's face toward [face] is a full shape. */
    fun isFaceSturdy(instance: Instance, position: Point, face: BlockFace): Boolean =
        isFaceFull(instance, position, face)

    /** Whether the block at [position] is a full/solid block. */
    fun isSolid(instance: Instance, position: Point): Boolean =
        instance.getBlockOrAir(position).solid()

    /** Whether the block can support a center-mounted block on [face]. */
    fun canSupportCenter(instance: Instance, position: Point, face: BlockFace): Boolean {
        val block = instance.getBlockOrAir(position)
        if (face == BlockFace.BOTTOM && block.key() in Tags.UNSTABLE_BOTTOM_CENTER) return false
        return isFaceFull(block, face)
    }

    /** Whether the block can support a rigid block above it. */
    fun canSupportRigidBlock(instance: Instance, position: Point): Boolean =
        isFaceFull(instance, position, BlockFace.TOP)

    /** Shapes that are full blocks but never connect to fences, walls, bars or panes. */
    fun isExceptionForConnection(block: Block): Boolean =
        block.key() in connectionExceptions || block.key() in Tags.SHULKER_BOXES || block.key() in Tags.LEAVES

    private val connectionExceptions: Set<Key> = buildSet {
        add(Key.key("minecraft:barrier"))
        add(Key.key("minecraft:carved_pumpkin"))
        add(Key.key("minecraft:jack_o_lantern"))
        add(Key.key("minecraft:melon"))
        add(Key.key("minecraft:pumpkin"))
    }
}

/**
 * A rectangle in the local 2D coordinates of a block face, bounded by [minU]/[maxU] and
 * [minV]/[maxV]. See [Support.faceRectangles] for which axes `u` and `v` are on each face.
 */
data class Rect(val minU: Double, val minV: Double, val maxU: Double, val maxV: Double)
