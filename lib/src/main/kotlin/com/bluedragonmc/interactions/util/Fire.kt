package com.bluedragonmc.interactions.util

import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace

/**
 * Computes fire block states.
 *
 * Fire stands on a sturdy or flammable block below, otherwise it attaches to its flammable
 * neighbors, and it becomes soul fire above soul sand or soul soil.
 */
object Fire {

    /**
     * The fire state that can survive at [position], or null when fire cannot be placed there.
     *
     * A sturdy or flammable block below makes the fire stand on its own; otherwise the fire attaches to
     * whichever of the other neighbors are flammable. [age] is preserved across updates.
     */
    fun stateAt(instance: Instance, position: BlockVec, age: String = "0"): Block? {
        val below = instance.getBlockOrAir(position.relative(BlockFace.BOTTOM))
        if (below.key() in Tags.SOUL_FIRE_BASE_BLOCKS) return Block.SOUL_FIRE
        if (below.key() in Flammable.BLOCKS || Support.isFaceFull(below, BlockFace.TOP)) {
            return Block.FIRE.withProperty(States.AGE, age)
        }

        var fire = Block.FIRE.withProperty(States.AGE, age)
        var attached = false
        for (face in CONNECTION_FACES) {
            val flammable = instance.getBlockOrAir(position.relative(face)).key() in Flammable.BLOCKS
            fire = fire.withProperty(States.connection(face), flammable.toString())
            attached = attached || flammable
        }
        return if (attached) fire else null
    }

    /** The faces fire sets connection properties for; the `down` side is never set. */
    private val CONNECTION_FACES = listOf(
        BlockFace.NORTH,
        BlockFace.EAST,
        BlockFace.SOUTH,
        BlockFace.WEST,
        BlockFace.TOP,
    )
}
