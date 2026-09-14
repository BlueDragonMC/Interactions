package com.bluedragonmc.interactions.rules.redstone

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
class RedstoneRulesTest {

    private fun player(env: Env, instance: Instance, yaw: Float = 0f) =
        env.createPlayer(instance, Pos(0.0, 40.0, 0.0, yaw, 0f))

    @Test
    fun `pressure plate sits on a supporting block`(env: Env) {
        val instance = setupInstance(env, PressurePlateRule())
        instance.setBlock(0, 39, 0, Block.STONE)

        val event = firePlace(instance, player(env, instance), Block.OAK_PRESSURE_PLATE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertFalse(event.isCancelled)
        assertEquals("false", event.block.getProperty("powered"))
    }

    @Test
    fun `rail follows the player's horizontal look`(env: Env) {
        val instance = setupInstance(env, RailRule())
        instance.setBlock(0, 39, 0, Block.STONE)

        val northSouth = firePlace(instance, player(env, instance, yaw = 0f), Block.RAIL, BlockFace.TOP, BlockVec(0, 40, 0))
        assertEquals("north_south", northSouth.block.getProperty("shape"))

        val eastWest = firePlace(instance, player(env, instance, yaw = 90f), Block.RAIL, BlockFace.TOP, BlockVec(0, 40, 1))
        assertEquals("east_west", eastWest.block.getProperty("shape"))
    }

    @Test
    fun `repeater faces away from the player`(env: Env) {
        val instance = setupInstance(env, RepeaterRule())
        instance.setBlock(0, 39, 0, Block.STONE)

        val event = firePlace(instance, player(env, instance), Block.REPEATER, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("north", event.block.getProperty("facing"))
    }
}
