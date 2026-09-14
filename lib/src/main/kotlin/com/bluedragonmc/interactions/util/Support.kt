package com.bluedragonmc.interactions.util

import net.kyori.adventure.key.Key
import net.minestom.server.coordinate.Point
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace

/**
 * Face/full-shape support checks.
 */
object Support {

    fun isFaceFull(instance: Instance, position: Point, face: BlockFace): Boolean =
        isFaceFull(instance.getBlockOrAir(position), face)

    fun isFaceFull(block: Block, face: BlockFace): Boolean =
        block.registry()?.collisionShape()?.isFaceFull(face) ?: false

    /** Whether the block's face toward [face] is a full shape. */
    fun isFaceSturdy(instance: Instance, position: Point, face: BlockFace): Boolean =
        isFaceFull(instance, position, face)

    /** Whether the block at [position] is a full/solid block. */
    fun isSolid(instance: Instance, position: Point): Boolean =
        instance.getBlockOrAir(position).isSolid

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
