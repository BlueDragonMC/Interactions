package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.PlayerHand
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
class StackingPlacementRuleTest {

    private class OakSlabRule : SlabRule(BlockSet.from(Block.OAK_SLAB))

    private class SnowRule : SnowLayerRule(BlockSet.from(Block.SNOW))

    private class CandleStackingRule : CandleRule(BlockSet.from(Block.CANDLE))

    private class SeaPickleStackingRule : SeaPickleRule(BlockSet.from(Block.SEA_PICKLE))

    private class TurtleEggStackingRule : TurtleEggRule(BlockSet.from(Block.TURTLE_EGG))

    private fun player(env: Env, instance: Instance) = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

    @Test
    fun `slab placed on a block becomes a bottom slab`(env: Env) {
        val instance = setupInstance(env, OakSlabRule())

        val event = firePlace(instance, player(env, instance), Block.OAK_SLAB, BlockFace.TOP, BlockVec(0, 40, 0))

        assertFalse(event.isCancelled)
        assertEquals(
            Block.OAK_SLAB.withProperty("type", "bottom").withProperty("waterlogged", "false"),
            event.block,
        )
    }

    @Test
    fun `slab placed high on a side face becomes a top slab`(env: Env) {
        val instance = setupInstance(env, OakSlabRule())

        val event = firePlace(
            instance,
            player(env, instance),
            Block.OAK_SLAB,
            BlockFace.EAST,
            BlockVec(0, 40, 0),
            Vec(0.5, 0.8, 0.5),
        )

        assertEquals(Block.OAK_SLAB.withProperty("type", "top"), event.block)
    }

    @Test
    fun `two slabs merge into a double slab`(env: Env) {
        val instance = setupInstance(env, OakSlabRule())
        instance.setBlock(0, 40, 0, Block.OAK_SLAB.withProperty("type", "bottom"))

        firePlace(instance, player(env, instance), Block.OAK_SLAB, BlockFace.TOP, BlockVec(0, 41, 0))

        assertEquals(
            Block.OAK_SLAB.withProperty("type", "double").withProperty("waterlogged", "false"),
            instance.getBlock(0, 40, 0),
        )
    }

    @Test
    fun `slabs do not merge when clicked low on the side`(env: Env) {
        val instance = setupInstance(env, OakSlabRule())
        instance.setBlock(0, 40, 0, Block.OAK_SLAB.withProperty("type", "bottom"))

        firePlace(
            instance,
            player(env, instance),
            Block.OAK_SLAB,
            BlockFace.EAST,
            BlockVec(1, 40, 0),
            Vec(0.5, 0.2, 0.5),
        )

        assertEquals(Block.OAK_SLAB.withProperty("type", "bottom"), instance.getBlock(0, 40, 0))
    }

    @Test
    fun `snow layers stack`(env: Env) {
        val instance = setupInstance(env, SnowRule())
        instance.setBlock(0, 40, 0, Block.SNOW)

        firePlace(instance, player(env, instance), Block.SNOW, BlockFace.TOP, BlockVec(0, 41, 0))

        assertEquals(Block.SNOW.withProperty("layers", "2"), instance.getBlock(0, 40, 0))
    }

    @Test
    fun `candles stack`(env: Env) {
        val instance = setupInstance(env, CandleStackingRule())
        instance.setBlock(0, 40, 0, Block.CANDLE)

        firePlace(instance, player(env, instance), Block.CANDLE, BlockFace.TOP, BlockVec(0, 41, 0))

        assertEquals(Block.CANDLE.withProperty("candles", "2"), instance.getBlock(0, 40, 0))
    }

    @Test
    fun `sea pickles stack`(env: Env) {
        val instance = setupInstance(env, SeaPickleStackingRule())
        instance.setBlock(0, 40, 0, Block.SEA_PICKLE)

        firePlace(instance, player(env, instance), Block.SEA_PICKLE, BlockFace.TOP, BlockVec(0, 41, 0))

        assertEquals(Block.SEA_PICKLE.withProperty("pickles", "2"), instance.getBlock(0, 40, 0))
    }

    @Test
    fun `turtle eggs stack`(env: Env) {
        val instance = setupInstance(env, TurtleEggStackingRule())
        instance.setBlock(0, 40, 0, Block.TURTLE_EGG)

        firePlace(instance, player(env, instance), Block.TURTLE_EGG, BlockFace.TOP, BlockVec(0, 41, 0))

        assertEquals(Block.TURTLE_EGG.withProperty("eggs", "2"), instance.getBlock(0, 40, 0))
    }

    @Test
    fun `candle is not placed without support`(env: Env) {
        val instance = setupInstance(env, CandleStackingRule())
        // Drop the player into the air so there is no support at (0, 40, 0).
        instance.setBlock(0, 39, 0, Block.AIR)

        val event = firePlace(instance, player(env, instance), Block.CANDLE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertTrue(event.isCancelled)
    }

    @Test
    fun `candles merge when the supporting block is clicked`(env: Env) {
        val instance = setupInstance(env, CandleStackingRule())
        instance.setBlock(0, 40, 0, Block.STONE)
        instance.setBlock(0, 41, 0, Block.CANDLE)

        val event = firePlace(instance, player(env, instance), Block.CANDLE, BlockFace.TOP, BlockVec(0, 41, 0))

        assertEquals("2", event.block.getProperty("candles"))
    }

    @Test
    fun `sea pickles merge when the supporting block is clicked`(env: Env) {
        val instance = setupInstance(env, SeaPickleStackingRule())
        instance.setBlock(0, 39, 0, Block.SAND)
        instance.setBlock(0, 40, 0, Block.SEA_PICKLE)

        val player = player(env, instance)
        holdBlock(player, Block.SEA_PICKLE)
        fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 39, 0))

        assertEquals("2", instance.getBlock(0, 40, 0).getProperty("pickles"))
        assertTrue(player.getItemInHand(PlayerHand.MAIN).isAir)
    }

    @Test
    fun `turtle eggs merge when the supporting block is clicked`(env: Env) {
        val instance = setupInstance(env, TurtleEggStackingRule())
        instance.setBlock(0, 39, 0, Block.SAND)
        instance.setBlock(0, 40, 0, Block.TURTLE_EGG)

        val player = player(env, instance)
        holdBlock(player, Block.TURTLE_EGG)
        fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 39, 0))

        assertEquals("2", instance.getBlock(0, 40, 0).getProperty("eggs"))
    }

    @Test
    fun `stacking blocks do not merge with a different item`(env: Env) {
        val instance = setupInstance(env, SeaPickleStackingRule())
        instance.setBlock(0, 39, 0, Block.SAND)
        instance.setBlock(0, 40, 0, Block.SEA_PICKLE)

        val player = player(env, instance)
        holdBlock(player, Block.STONE)
        fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 39, 0))

        assertEquals("1", instance.getBlock(0, 40, 0).getProperty("pickles"))
        assertFalse(player.getItemInHand(PlayerHand.MAIN).isAir)
    }

    @Test
    fun `snow layer against the side of a block lands beside it`(env: Env) {
        val instance = setupInstance(env, SnowRule())
        instance.setBlock(0, 40, 2, Block.STONE)

        simulatePlace(player(env, instance), Block.SNOW, BlockVec(0, 40, 2), BlockFace.NORTH)

        assertEquals("1", instance.getBlock(0, 40, 1).getProperty("layers"))
    }

    @Test
    fun `snow layer against the side of a snow layer lands beside it`(env: Env) {
        val instance = setupInstance(env, SnowRule())
        instance.setBlock(0, 40, 2, Block.SNOW)

        simulatePlace(player(env, instance), Block.SNOW, BlockVec(0, 40, 2), BlockFace.NORTH)

        assertEquals("1", instance.getBlock(0, 40, 1).getProperty("layers"))
        assertEquals("1", instance.getBlock(0, 40, 2).getProperty("layers"))
    }

    @Test
    fun `snow layer against the side of another snow layer merges into it`(env: Env) {
        val instance = setupInstance(env, SnowRule())
        instance.setBlock(0, 40, 1, Block.SNOW)
        instance.setBlock(0, 40, 2, Block.SNOW)

        simulatePlace(player(env, instance), Block.SNOW, BlockVec(0, 40, 2), BlockFace.NORTH)

        assertEquals("2", instance.getBlock(0, 40, 1).getProperty("layers"))
        assertEquals("1", instance.getBlock(0, 40, 2).getProperty("layers"))
    }

    @Test
    fun `sea pickle against the side of a full stack lands beside it`(env: Env) {
        val instance = setupInstance(env, SeaPickleStackingRule())
        instance.setBlock(0, 40, 2, Block.SEA_PICKLE.withProperty("pickles", "4").withProperty("waterlogged", "false"))

        simulatePlace(player(env, instance), Block.SEA_PICKLE, BlockVec(0, 40, 2), BlockFace.NORTH)

        assertEquals("1", instance.getBlock(0, 40, 1).getProperty("pickles"))
        assertEquals("4", instance.getBlock(0, 40, 2).getProperty("pickles"))
    }
}
