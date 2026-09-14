package com.bluedragonmc.interactions.rules.plants

import com.bluedragonmc.interactions.rules.base.firePlace
import com.bluedragonmc.interactions.rules.base.setupInstance
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Pos
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
class PlantRulesTest {

    private fun player(env: Env, instance: Instance) = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

    @Test
    fun `crop grows on farmland`(env: Env) {
        val instance = setupInstance(env, CropRule())
        instance.setBlock(0, 39, 0, Block.FARMLAND)

        val event = firePlace(instance, player(env, instance), Block.WHEAT, BlockFace.TOP, BlockVec(0, 40, 0))

        assertFalse(event.isCancelled)
        assertEquals("0", event.block.getProperty("age"))
    }

    @Test
    fun `crop is cancelled without farmland`(env: Env) {
        val instance = setupInstance(env, CropRule())
        instance.setBlock(0, 39, 0, Block.STONE)

        val event = firePlace(instance, player(env, instance), Block.WHEAT, BlockFace.TOP, BlockVec(0, 40, 0))

        assertTrue(event.isCancelled)
    }

    @Test
    fun `sapling grows on grass`(env: Env) {
        val instance = setupInstance(env, SaplingRule())
        instance.setBlock(0, 39, 0, Block.GRASS_BLOCK)

        val event = firePlace(instance, player(env, instance), Block.OAK_SAPLING, BlockFace.TOP, BlockVec(0, 40, 0))

        assertFalse(event.isCancelled)
    }

    @Test
    fun `cactus grows on sand`(env: Env) {
        val instance = setupInstance(env, CactusRule())
        instance.setBlock(0, 39, 0, Block.SAND)

        val event = firePlace(instance, player(env, instance), Block.CACTUS, BlockFace.TOP, BlockVec(0, 40, 0))

        assertFalse(event.isCancelled)
        assertEquals(Block.CACTUS.key(), event.block.key())
    }

    @Test
    fun `cactus is not placed next to a solid block`(env: Env) {
        val instance = setupInstance(env, CactusRule())
        instance.setBlock(0, 39, 0, Block.SAND)
        instance.setBlock(1, 40, 0, Block.STONE)

        val event = firePlace(instance, player(env, instance), Block.CACTUS, BlockFace.TOP, BlockVec(0, 40, 0))

        assertTrue(event.isCancelled)
    }

    @Test
    fun `cactus is destroyed when a solid block appears next to it`(env: Env) {
        val instance = setupInstance(env, CactusRule())
        instance.setBlock(0, 39, 0, Block.SAND)
        instance.setBlock(0, 40, 0, Block.CACTUS)

        instance.setBlock(1, 40, 0, Block.STONE)

        assertEquals(Block.AIR, instance.getBlock(0, 40, 0))
    }

    @Test
    fun `sugar cane grows next to water`(env: Env) {
        val instance = setupInstance(env, SugarCaneRule())
        instance.setBlock(0, 39, 0, Block.SAND)
        instance.setBlock(1, 39, 0, Block.WATER)

        val event = firePlace(instance, player(env, instance), Block.SUGAR_CANE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertFalse(event.isCancelled)
    }

    @Test
    fun `coral requires water and support`(env: Env) {
        val instance = setupInstance(env, CoralRule())
        instance.setBlock(0, 39, 0, Block.STONE)
        instance.setBlock(0, 40, 0, Block.WATER)

        val event = firePlace(instance, player(env, instance), Block.BRAIN_CORAL, BlockFace.TOP, BlockVec(0, 40, 0))

        assertFalse(event.isCancelled)
        assertEquals("true", event.block.getProperty("waterlogged"))
    }

    @Test
    fun `vine attaches to a neighbouring block`(env: Env) {
        val instance = setupInstance(env, VineRule())
        instance.setBlock(0, 40, 1, Block.STONE)

        val event = firePlace(instance, player(env, instance), Block.VINE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertFalse(event.isCancelled)
        assertEquals("true", event.block.getProperty("south"))
    }
}
