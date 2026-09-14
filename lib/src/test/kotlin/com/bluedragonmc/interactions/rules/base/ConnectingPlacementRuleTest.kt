package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.util.Support
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.utils.Direction
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@EnvTest
class ConnectingPlacementRuleTest {

    private class FenceRule : ConnectingPlacementRule(BlockSet.from(Block.OAK_FENCE)) {
        override fun canConnect(neighbor: Block, faceSolid: Boolean, direction: Direction): Boolean =
            (!Support.isExceptionForConnection(neighbor) && faceSolid) || neighbor.key() in keys
    }

    @Test
    fun `fence connects to a solid neighbor`(env: Env) {
        val instance = setupInstance(env, FenceRule())
        instance.setBlock(0, 40, 1, Block.STONE)
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

        val event = firePlace(instance, player, Block.OAK_FENCE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("false", event.block.getProperty("north"))
        assertEquals("false", event.block.getProperty("east"))
        assertEquals("true", event.block.getProperty("south"))
        assertEquals("false", event.block.getProperty("west"))
        assertEquals("false", event.block.getProperty("waterlogged"))
    }

    @Test
    fun `fence connects to another fence`(env: Env) {
        val instance = setupInstance(env, FenceRule())
        instance.setBlock(0, 40, 1, Block.OAK_FENCE)
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

        val event = firePlace(instance, player, Block.OAK_FENCE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("true", event.block.getProperty("south"))
    }

    @Test
    fun `connection updates when a neighbor changes`(env: Env) {
        val instance = setupInstance(env, FenceRule())
        instance.setBlock(0, 40, 0, Block.OAK_FENCE)

        instance.setBlock(0, 40, 1, Block.STONE)
        assertEquals("true", instance.getBlock(0, 40, 0).getProperty("south"))

        instance.setBlock(0, 40, 1, Block.AIR)
        assertEquals("false", instance.getBlock(0, 40, 0).getProperty("south"))
    }
}
