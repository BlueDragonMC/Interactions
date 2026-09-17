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
     * Stricter than [isFaceFull]
     * (a top slab has a full top face but is not a full block).
     */
    fun isFullBlock(block: Block): Boolean {
        val boxes = collisionBoxes(block)
        if (boxes.size != 1) return false
        val box = boxes[0]
        return box.minX() <= EPSILON && box.minY() <= EPSILON && box.minZ() <= EPSILON &&
            box.maxX() >= 1.0 - EPSILON && box.maxY() >= 1.0 - EPSILON && box.maxZ() >= 1.0 - EPSILON
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
