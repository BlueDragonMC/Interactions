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

    /** Whether the block can support a center-mounted block on [face]. */
    fun canSupportCenter(instance: Instance, position: Point, face: BlockFace): Boolean {
        val block = instance.getBlockOrAir(position)
        if (face == BlockFace.BOTTOM && block.key() in unstableBottomCenter) return false
        return isFaceFull(block, face)
    }

    /** Whether the block can support a rigid block above it. */
    fun canSupportRigidBlock(instance: Instance, position: Point): Boolean =
        isFaceFull(instance, position, BlockFace.TOP)

    /** Shapes that are full blocks but never connect to fences, walls, bars or panes. */
    fun isExceptionForConnection(block: Block): Boolean =
        block.key() in connectionExceptions || block.key() in shulkerBoxes || block.key() in leaves

    private val unstableBottomCenter: Set<Key> by lazy { tag("minecraft:unstable_bottom_center") }

    private val connectionExceptions: Set<Key> = buildSet {
        add(Key.key("minecraft:barrier"))
        add(Key.key("minecraft:carved_pumpkin"))
        add(Key.key("minecraft:jack_o_lantern"))
        add(Key.key("minecraft:melon"))
        add(Key.key("minecraft:pumpkin"))
    }

    private val shulkerBoxes: Set<Key> by lazy { tag("minecraft:shulker_boxes") }

    private val leaves: Set<Key> by lazy { tag("minecraft:leaves") }

    private fun tag(key: String): Set<Key> =
        Block.staticRegistry().getTag(Key.key(key))
            ?.mapTo(mutableSetOf()) { it.key() }
            ?: emptySet()
}
