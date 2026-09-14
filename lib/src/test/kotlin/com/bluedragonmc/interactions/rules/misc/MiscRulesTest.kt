package com.bluedragonmc.interactions.rules.misc

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
}
