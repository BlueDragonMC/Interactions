package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@EnvTest
class WallMountedPlacementRuleTest {

    private class ButtonRule : FaceAttachedPlacementRule(BlockSet.from(Block.OAK_BUTTON))

    @Test
    fun `face attached block mounts on the floor`(env: Env) {
        val instance = setupInstance(env, ButtonRule())
        // Looking straight down selects the block below as the support.
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 90f))

        val event = firePlace(instance, player, Block.OAK_BUTTON, BlockFace.TOP, BlockVec(0, 40, 0))

        assertFalse(event.isCancelled)
        assertEquals(
            Block.OAK_BUTTON.withProperty("facing", "south").withProperty("face", "floor"),
            event.block,
        )
    }

    @Test
    fun `face attached block mounts on the ceiling`(env: Env) {
        val instance = setupInstance(env, ButtonRule())
        instance.setBlock(0, 41, 0, Block.STONE)
        // Looking straight up selects the block above as the support.
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, -90f))

        val event = firePlace(instance, player, Block.OAK_BUTTON, BlockFace.BOTTOM, BlockVec(0, 40, 0))

        assertFalse(event.isCancelled)
        assertEquals(
            Block.OAK_BUTTON.withProperty("facing", "south").withProperty("face", "ceiling"),
            event.block,
        )
    }

    @Test
    fun `block is destroyed when its support is removed`(env: Env) {
        val instance = setupInstance(env, ButtonRule())
        instance.setBlock(0, 40, 1, Block.STONE)
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

        val placed = firePlace(instance, player, Block.OAK_BUTTON, BlockFace.NORTH, BlockVec(0, 40, 0))
        assertEquals("wall", placed.block.getProperty("face"))
        instance.setBlock(0, 40, 0, placed.block)

        instance.setBlock(0, 40, 1, Block.AIR)

        assertEquals(Block.AIR, instance.getBlock(0, 40, 0))
    }
}
