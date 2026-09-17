package com.bluedragonmc.interactions.rules.connecting

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
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

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
    fun `wall marks a tall arm and raises its post under a full block`(env: Env) {
        val instance = setupInstance(env, WallRule())
        instance.setBlock(0, 40, 1, Block.STONE)
        instance.setBlock(0, 41, 0, Block.STONE)

        val event = firePlace(instance, player(env, instance), Block.COBBLESTONE_WALL, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("tall", event.block.getProperty("south"))
        assertEquals("true", event.block.getProperty("up"))
    }

    @Test
    fun `wall raises its post under a post override`(env: Env) {
        val instance = setupInstance(env, WallRule())
        instance.setBlock(0, 40, 1, Block.STONE)
        instance.setBlock(0, 40, -1, Block.STONE)
        instance.setBlock(0, 41, 0, Block.TORCH)

        val event = firePlace(instance, player(env, instance), Block.COBBLESTONE_WALL, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("low", event.block.getProperty("south"))
        assertEquals("true", event.block.getProperty("up"))
    }

    @Test
    fun `wall arms are low without a covering block above`(env: Env) {
        val instance = setupInstance(env, WallRule())
        instance.setBlock(0, 40, 1, Block.STONE)

        val event = firePlace(instance, player(env, instance), Block.COBBLESTONE_WALL, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("low", event.block.getProperty("south"))
    }

    /**
     * A solid placed after the wall it supports must still make the wall below the connected wall
     * report a tall arm. The change has to travel two blocks, which used to be blocked by the wall's
     * update distance.
     */
    @Test
    fun `placing the solids after the walls still updates the lower wall`(env: Env) {
        val instance = setupInstance(env, WallRule())
        val player = player(env, instance)

        // Walls first, stacked on the ground.
        simulatePlace(player, Block.COBBLESTONE_WALL, BlockVec(1, 39, 0), BlockFace.TOP)
        simulatePlace(player, Block.COBBLESTONE_WALL, BlockVec(1, 40, 0), BlockFace.TOP)
        assertEquals("none", instance.getBlock(1, 40, 0).getProperty("west"))

        // Solids second.
        instance.setBlock(0, 40, 0, Block.STONE)
        instance.setBlock(0, 41, 0, Block.STONE)

        assertEquals("tall", instance.getBlock(1, 40, 0).getProperty("west"))
    }

    @Test
    fun `placing the solids before the walls updates the lower wall`(env: Env) {
        val instance = setupInstance(env, WallRule())
        val player = player(env, instance)

        instance.setBlock(0, 40, 0, Block.STONE)
        instance.setBlock(0, 41, 0, Block.STONE)

        simulatePlace(player, Block.COBBLESTONE_WALL, BlockVec(0, 40, 0), BlockFace.EAST)
        simulatePlace(player, Block.COBBLESTONE_WALL, BlockVec(0, 41, 0), BlockFace.EAST)

        assertEquals("tall", instance.getBlock(1, 40, 0).getProperty("west"))
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

    @Test
    fun `clicking a fence gate opens and closes it`(env: Env) {
        val instance = setupInstance(env, FenceGateRule())
        instance.setBlock(0, 40, 0, Block.OAK_FENCE_GATE.withProperty("open", "false"))
        val player = player(env, instance)

        val event = fireInteract(instance, player, Block.OAK_FENCE_GATE, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertEquals("true", instance.getBlock(0, 40, 0).getProperty("open"))
        assertTrue(event.isBlockingItemUse)
    }

    @Test
    fun `fence gate opens toward the player`(env: Env) {
        val instance = setupInstance(env, FenceGateRule())
        // Facing north while the player (yaw 0) faces south, so the gate flips to face south.
        instance.setBlock(0, 40, 0, Block.OAK_FENCE_GATE.withProperty("open", "false").withProperty("facing", "north"))
        val player = player(env, instance)

        fireInteract(instance, player, Block.OAK_FENCE_GATE, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertEquals("south", instance.getBlock(0, 40, 0).getProperty("facing"))
        assertEquals("true", instance.getBlock(0, 40, 0).getProperty("open"))
    }

    @Test
    fun `shift clicking a fence gate leaves it closed`(env: Env) {
        val instance = setupInstance(env, FenceGateRule())
        instance.setBlock(0, 40, 0, Block.OAK_FENCE_GATE.withProperty("open", "false"))
        val player = player(env, instance)
        player.setSneaking(true)
        holdBlock(player, Block.STONE)

        val event = fireInteract(instance, player, Block.OAK_FENCE_GATE, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertEquals("false", instance.getBlock(0, 40, 0).getProperty("open"))
        assertFalse(event.isBlockingItemUse)
    }
}
