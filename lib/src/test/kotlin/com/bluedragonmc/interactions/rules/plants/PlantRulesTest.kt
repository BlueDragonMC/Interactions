package com.bluedragonmc.interactions.rules.plants

import com.bluedragonmc.interactions.rules.base.fireInteract
import com.bluedragonmc.interactions.rules.base.firePlace
import com.bluedragonmc.interactions.rules.base.holdBlock
import com.bluedragonmc.interactions.rules.base.setupInstance
import com.bluedragonmc.interactions.rules.base.simulatePlace
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

    private fun player(env: Env, instance: Instance, yaw: Float = 0f, pitch: Float = 0f) =
        env.createPlayer(instance, Pos(0.0, 40.0, 0.0, yaw, pitch))

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

    // --- Flower beds ---------------------------------------------------------------------------

    @Test
    fun `wildflowers merge when clicked`(env: Env) {
        val instance = setupInstance(env, FlowerBedRule())
        instance.setBlock(0, 39, 0, Block.GRASS_BLOCK)
        instance.setBlock(0, 40, 0, Block.WILDFLOWERS)

        firePlace(instance, player(env, instance), Block.WILDFLOWERS, BlockFace.TOP, BlockVec(0, 41, 0))

        assertEquals("2", instance.getBlock(0, 40, 0).getProperty("flower_amount"))
        assertEquals(Block.AIR, instance.getBlock(0, 41, 0))
    }

    @Test
    fun `wildflowers can be placed on grass`(env: Env) {
        val instance = setupInstance(env, FlowerBedRule())
        instance.setBlock(0, 39, 0, Block.GRASS_BLOCK)

        val event = firePlace(instance, player(env, instance), Block.WILDFLOWERS, BlockFace.TOP, BlockVec(0, 40, 0))

        assertFalse(event.isCancelled)
    }

    @Test
    fun `wildflowers face the player`(env: Env) {
        val instance = setupInstance(env, FlowerBedRule())
        instance.setBlock(0, 39, 0, Block.GRASS_BLOCK)

        // yaw 0 looks south, so the bed faces the opposite way (north).
        val event = firePlace(instance, player(env, instance), Block.WILDFLOWERS, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("north", event.block.getProperty("facing"))
    }

    @Test
    fun `wildflowers merge when the supporting block is clicked`(env: Env) {
        val instance = setupInstance(env, FlowerBedRule())
        instance.setBlock(0, 39, 0, Block.GRASS_BLOCK)
        instance.setBlock(0, 40, 0, Block.WILDFLOWERS)

        val player = player(env, instance)
        holdBlock(player, Block.WILDFLOWERS)
        fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 39, 0))

        assertEquals("2", instance.getBlock(0, 40, 0).getProperty("flower_amount"))
    }

    @Test
    fun `wildflower against the side of a full bed lands beside it`(env: Env) {
        val instance = setupInstance(env, FlowerBedRule())
        instance.setBlock(0, 39, 1, Block.GRASS_BLOCK)
        instance.setBlock(0, 39, 2, Block.GRASS_BLOCK)
        instance.setBlock(0, 40, 2, Block.WILDFLOWERS.withProperty("flower_amount", "4"))

        simulatePlace(player(env, instance), Block.WILDFLOWERS, BlockVec(0, 40, 2), BlockFace.NORTH)

        assertEquals("1", instance.getBlock(0, 40, 1).getProperty("flower_amount"))
        assertEquals("4", instance.getBlock(0, 40, 2).getProperty("flower_amount"))
    }

    @Test
    fun `wildflower against the side of a non-full bed merges into it`(env: Env) {
        val instance = setupInstance(env, FlowerBedRule())
        instance.setBlock(0, 39, 1, Block.GRASS_BLOCK)
        instance.setBlock(0, 39, 2, Block.GRASS_BLOCK)
        instance.setBlock(0, 40, 2, Block.WILDFLOWERS)

        simulatePlace(player(env, instance), Block.WILDFLOWERS, BlockVec(0, 40, 2), BlockFace.NORTH)

        assertEquals(Block.AIR, instance.getBlock(0, 40, 1))
        assertEquals("2", instance.getBlock(0, 40, 2).getProperty("flower_amount"))
    }

    // --- Cactus --------------------------------------------------------------------------------

    @Test
    fun `cactus can be placed on another cactus`(env: Env) {
        val instance = setupInstance(env, CactusRule())
        instance.setBlock(0, 39, 0, Block.SAND)
        instance.setBlock(0, 40, 0, Block.CACTUS)

        val event = firePlace(instance, player(env, instance), Block.CACTUS, BlockFace.TOP, BlockVec(0, 41, 0))

        assertFalse(event.isCancelled)
    }

    // --- Dripstone and sulfur ------------------------------------------------------------------

    @Test
    fun `dripstone is not placed without support`(env: Env) {
        val instance = setupInstance(env, PointedDripstoneRule())
        instance.setBlock(0, 39, 0, Block.AIR)

        val event = firePlace(
            instance,
            player(env, instance, pitch = 45f),
            Block.POINTED_DRIPSTONE,
            BlockFace.TOP,
            BlockVec(0, 40, 0),
        )

        assertTrue(event.isCancelled)
    }

    @Test
    fun `dripstone is removed when its support disappears`(env: Env) {
        val instance = setupInstance(env, PointedDripstoneRule())
        instance.setBlock(0, 39, 0, Block.STONE)
        instance.setBlock(
            0, 40, 0,
            Block.POINTED_DRIPSTONE.withProperty("vertical_direction", "up").withProperty("thickness", "tip"),
        )

        instance.setBlock(0, 39, 0, Block.AIR)

        assertEquals(Block.AIR, instance.getBlock(0, 40, 0))
    }

    @Test
    fun `sulfur spikes connect to each other`(env: Env) {
        val instance = setupInstance(env, SulfurSpikeRule())
        instance.setBlock(0, 39, 0, Block.STONE)
        instance.setBlock(
            0, 40, 0,
            Block.SULFUR_SPIKE.withProperty("vertical_direction", "up").withProperty("thickness", "tip"),
        )
        instance.setBlock(
            0, 41, 0,
            Block.SULFUR_SPIKE.withProperty("vertical_direction", "up").withProperty("thickness", "tip"),
        )

        assertEquals("frustum", instance.getBlock(0, 40, 0).getProperty("thickness"))
    }

    // --- Sugar cane ----------------------------------------------------------------------------

    @Test
    fun `removing the bottom sugar cane removes the whole tower`(env: Env) {
        val instance = setupInstance(env, SugarCaneRule())
        instance.setBlock(0, 39, 0, Block.SAND)
        instance.setBlock(1, 39, 0, Block.WATER)
        instance.setBlock(0, 40, 0, Block.SUGAR_CANE)
        instance.setBlock(0, 41, 0, Block.SUGAR_CANE)
        instance.setBlock(0, 42, 0, Block.SUGAR_CANE)

        instance.setBlock(0, 40, 0, Block.AIR)

        assertEquals(Block.AIR, instance.getBlock(0, 41, 0))
        assertEquals(Block.AIR, instance.getBlock(0, 42, 0))
    }
}
