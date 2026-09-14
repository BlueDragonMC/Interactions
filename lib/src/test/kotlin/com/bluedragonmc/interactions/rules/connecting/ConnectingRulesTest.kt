package com.bluedragonmc.interactions.rules.connecting

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
import kotlin.test.assertNotEquals

@EnvTest
class ConnectingRulesTest {

    private fun player(env: Env, instance: Instance, yaw: Float = 0f) =
        env.createPlayer(instance, Pos(0.0, 40.0, 0.0, yaw, 0f))

    @Test
    fun `wooden fence connects to a solid neighbour`(env: Env) {
        val instance = setupInstance(env, WoodenFenceRule())
        instance.setBlock(0, 40, 1, Block.STONE)

        val event = firePlace(instance, player(env, instance), Block.OAK_FENCE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("true", event.block.getProperty("south"))
        assertEquals("false", event.block.getProperty("north"))
    }

    @Test
    fun `iron bars connect to a glass pane`(env: Env) {
        val instance = setupInstance(env, IronBarsRule())
        instance.setBlock(0, 40, 1, Block.GLASS_PANE)

        val event = firePlace(instance, player(env, instance), Block.IRON_BARS, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("true", event.block.getProperty("south"))
    }

    @Test
    fun `wall connects and raises its post`(env: Env) {
        val instance = setupInstance(env, WallRule())
        instance.setBlock(0, 40, 1, Block.STONE)

        val event = firePlace(instance, player(env, instance), Block.COBBLESTONE_WALL, BlockFace.TOP, BlockVec(0, 40, 0))

        assertNotEquals("none", event.block.getProperty("south"))
        assertEquals("true", event.block.getProperty("up"))
    }

    @Test
    fun `chain takes its axis from the clicked face`(env: Env) {
        val instance = setupInstance(env, ChainRule())

        val event = firePlace(instance, player(env, instance), Block.IRON_CHAIN, BlockFace.EAST, BlockVec(0, 40, 0))

        assertEquals("x", event.block.getProperty("axis"))
    }

    @Test
    fun `fence gate faces the player`(env: Env) {
        val instance = setupInstance(env, FenceGateRule())

        val event = firePlace(instance, player(env, instance), Block.OAK_FENCE_GATE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("south", event.block.getProperty("facing"))
        assertEquals("false", event.block.getProperty("powered"))
    }
}
