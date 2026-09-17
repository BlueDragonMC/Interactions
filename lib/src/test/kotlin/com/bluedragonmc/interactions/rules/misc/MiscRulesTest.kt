package com.bluedragonmc.interactions.rules.misc

import com.bluedragonmc.interactions.rules.base.fireInteract
import com.bluedragonmc.interactions.rules.base.firePlace
import com.bluedragonmc.interactions.rules.base.holdMaterial
import com.bluedragonmc.interactions.rules.base.setupInstance
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.item.Material
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@EnvTest
class MiscRulesTest {

    private fun player(env: Env, instance: Instance, yaw: Float = 0f) =
        env.createPlayer(instance, Pos(0.0, 40.0, 0.0, yaw, 0f))

    @Test
    fun `chest faces the player`(env: Env) {
        val instance = setupInstance(env, ChestRule())

        val event = firePlace(instance, player(env, instance), Block.CHEST, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("north", event.block.getProperty("facing"))
    }

    @Test
    fun `shulker box faces the clicked face`(env: Env) {
        val instance = setupInstance(env, ShulkerBoxRule())

        val event = firePlace(instance, player(env, instance), Block.SHULKER_BOX, BlockFace.EAST, BlockVec(0, 40, 0))

        assertEquals("east", event.block.getProperty("facing"))
    }

    @Test
    fun `hopper points down when placed on top of a block`(env: Env) {
        val instance = setupInstance(env, HopperRule())
        instance.setBlock(0, 39, 0, Block.STONE)

        val event = firePlace(instance, player(env, instance), Block.HOPPER, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("down", event.block.getProperty("facing"))
        assertEquals("true", event.block.getProperty("enabled"))
    }

    @Test
    fun `anvil faces clockwise from the player`(env: Env) {
        val instance = setupInstance(env, AnvilRule())

        val event = firePlace(instance, player(env, instance), Block.ANVIL, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("west", event.block.getProperty("facing"))
    }

    @Test
    fun `scaffolding rests on its support`(env: Env) {
        val instance = setupInstance(env, ScaffoldingRule())
        instance.setBlock(0, 39, 0, Block.STONE)

        val event = firePlace(instance, player(env, instance), Block.SCAFFOLDING, BlockFace.TOP, BlockVec(0, 40, 0))

        assertFalse(event.isCancelled)
        assertEquals("0", event.block.getProperty("distance"))
    }

    @Test
    fun `a chest placed beside a matching chest becomes the other half`(env: Env) {
        val instance = setupInstance(env, ChestRule())
        // yaw 0 faces south, so a placed chest faces north; match the existing chest to it.
        instance.setBlock(0, 40, 0, Block.CHEST.withProperty("facing", "north").withProperty("type", "single"))

        val placed = firePlace(instance, player(env, instance), Block.CHEST, BlockFace.TOP, BlockVec(1, 40, 0))
        assertEquals("right", placed.block.getProperty("type"))

        instance.setBlock(1, 40, 0, placed.block)

        assertEquals("left", instance.getBlock(0, 40, 0).getProperty("type"))
    }

    @Test
    fun `carpet is destroyed when the block below is removed`(env: Env) {
        val instance = setupInstance(env, CarpetRule())
        instance.setBlock(0, 39, 0, Block.STONE)
        instance.setBlock(0, 40, 0, Block.WHITE_CARPET)

        instance.setBlock(0, 39, 0, Block.AIR)

        assertEquals(Block.AIR, instance.getBlock(0, 40, 0))
    }

    @Test
    fun `leaf litter merges when the supporting block is clicked`(env: Env) {
        val instance = setupInstance(env, LeafLitterRule())
        instance.setBlock(0, 39, 0, Block.GRASS_BLOCK)
        instance.setBlock(0, 40, 0, Block.LEAF_LITTER)

        // Leaf litter is replaceable, so Minestom fires a normal placement event instead of rejecting it.
        val event = firePlace(instance, player(env, instance), Block.LEAF_LITTER, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("2", event.block.getProperty("segment_amount"))
    }

    @Test
    fun `leaf litter can be placed on a non-vegetation block`(env: Env) {
        val instance = setupInstance(env, LeafLitterRule())
        instance.setBlock(0, 39, 0, Block.STONE)

        val event = firePlace(instance, player(env, instance), Block.LEAF_LITTER, BlockFace.TOP, BlockVec(0, 40, 0))

        assertFalse(event.isCancelled)
    }

    @Test
    fun `flint and steel lights a candle`(env: Env) {
        val instance = setupInstance(env, CandlePlacementRule())
        instance.setBlock(0, 40, 0, Block.CANDLE)
        val player = player(env, instance)
        holdMaterial(player, Material.FLINT_AND_STEEL)

        val event = fireInteract(instance, player, Block.CANDLE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("true", instance.getBlock(0, 40, 0).getProperty("lit"))
        assertTrue(event.isBlockingItemUse)
    }

    @Test
    fun `waterlogged candles cannot be lit`(env: Env) {
        val instance = setupInstance(env, CandlePlacementRule())
        val waterlogged = Block.CANDLE.withProperty("waterlogged", "true")
        instance.setBlock(0, 40, 0, waterlogged)
        val player = player(env, instance)
        holdMaterial(player, Material.FLINT_AND_STEEL)

        val event = fireInteract(instance, player, waterlogged, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("false", instance.getBlock(0, 40, 0).getProperty("lit"))
        assertFalse(event.isBlockingItemUse)
    }
}
