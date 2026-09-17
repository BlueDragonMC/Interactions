package com.bluedragonmc.interactions.rules.structural

import com.bluedragonmc.interactions.rules.base.fireInteract
import com.bluedragonmc.interactions.rules.base.firePlace
import com.bluedragonmc.interactions.rules.base.setupInstance
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@EnvTest
class StructuralPlacementRulesTest {

    private fun player(env: Env, instance: Instance) = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

    @Test
    fun `slab tag rule merges two slabs`(env: Env) {
        val instance = setupInstance(env, SlabPlacementRule())
        instance.setBlock(0, 40, 0, Block.STONE_SLAB.withProperty("type", "bottom"))

        firePlace(instance, player(env, instance), Block.STONE_SLAB, BlockFace.TOP, BlockVec(0, 41, 0))

        assertEquals(
            Block.STONE_SLAB.withProperty("type", "double").withProperty("waterlogged", "false"),
            instance.getBlock(0, 40, 0),
        )
    }

    @Test
    fun `stair tag rule faces the player and builds a straight bottom stair`(env: Env) {
        val instance = setupInstance(env, StairsPlacementRule())

        val event = firePlace(instance, player(env, instance), Block.STONE_STAIRS, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(
            Block.STONE_STAIRS
                .withProperty("facing", "south")
                .withProperty("half", "bottom")
                .withProperty("shape", "straight")
                .withProperty("waterlogged", "false"),
            event.block,
        )
    }

    @Test
    fun `trapdoor on a horizontal face takes its facing from the click`(env: Env) {
        val instance = setupInstance(env, TrapdoorRule())
        instance.setBlock(-1, 40, 0, Block.STONE)

        val event = firePlace(
            instance,
            player(env, instance),
            Block.OAK_TRAPDOOR,
            BlockFace.EAST,
            BlockVec(0, 40, 0),
            Vec(0.5, 0.8, 0.5),
        )

        assertEquals(
            Block.OAK_TRAPDOOR
                .withProperty("facing", "east")
                .withProperty("half", "top")
                .withProperty("open", "false")
                .withProperty("powered", "false")
                .withProperty("waterlogged", "false"),
            event.block,
        )
    }

    @Test
    fun `trapdoor on top of a block faces the player`(env: Env) {
        val instance = setupInstance(env, TrapdoorRule())
        instance.setBlock(0, 39, 0, Block.STONE)

        val event = firePlace(instance, player(env, instance), Block.OAK_TRAPDOOR, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("north", event.block.getProperty("facing"))
        assertEquals("bottom", event.block.getProperty("half"))
    }

    @Test
    fun `trapdoor opens when placed next to a powered block`(env: Env) {
        val instance = setupInstance(env, TrapdoorRule())
        instance.setBlock(0, 39, 0, Block.STONE)
        instance.setBlock(1, 40, 0, Block.REDSTONE_WIRE.withProperty("power", "15"))

        val event = firePlace(instance, player(env, instance), Block.OAK_TRAPDOOR, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("true", event.block.getProperty("open"))
        assertEquals("true", event.block.getProperty("powered"))
    }

    @Test
    fun `clicking a trapdoor toggles it`(env: Env) {
        val instance = setupInstance(env, TrapdoorRule())
        instance.setBlock(0, 40, 0, Block.OAK_TRAPDOOR)
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

        val event = fireInteract(instance, player, Block.OAK_TRAPDOOR, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertEquals("true", instance.getBlock(0, 40, 0).getProperty("open"))
        assertTrue(event.isBlockingItemUse)
    }

    @Test
    fun `iron trapdoors do not open by hand`(env: Env) {
        val instance = setupInstance(env, TrapdoorRule())
        instance.setBlock(0, 40, 0, Block.IRON_TRAPDOOR)
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

        val event = fireInteract(instance, player, Block.IRON_TRAPDOOR, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertEquals("false", instance.getBlock(0, 40, 0).getProperty("open"))
        assertFalse(event.isBlockingItemUse)
    }
}
