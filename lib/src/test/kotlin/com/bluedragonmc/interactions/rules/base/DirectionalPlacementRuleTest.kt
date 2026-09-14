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
import kotlin.test.assertTrue

@EnvTest
class DirectionalPlacementRuleTest {

    private class FurnaceRule : HorizontalFacingRule(BlockSet.from(Block.FURNACE))

    private class AwayFacingRule : HorizontalFacingRule(BlockSet.from(Block.OAK_STAIRS), facesPlayer = false)

    private class LogRule : AllAxisRule(BlockSet.from(Block.OAK_LOG))

    private class ClusterRule : ClickFacingRule(BlockSet.from(Block.AMETHYST_CLUSTER))

    private class EndRodRule : VerticalFacingRule(BlockSet.from(Block.END_ROD))

    private class BarrelRule : NearestFacingRule(BlockSet.from(Block.BARREL))

    @Test
    fun `horizontal facing rule faces the player`(env: Env) {
        val instance = setupInstance(env, FurnaceRule())
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

        val event = firePlace(instance, player, Block.FURNACE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertFalse(event.isCancelled)
        assertEquals(Block.FURNACE.withProperty("facing", "north"), event.block)
    }

    @Test
    fun `horizontal facing rule follows the yaw`(env: Env) {
        val instance = setupInstance(env, FurnaceRule())
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 90f, 0f))

        val event = firePlace(instance, player, Block.FURNACE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.FURNACE.withProperty("facing", "east"), event.block)
    }

    @Test
    fun `horizontal facing rule can face away from the player`(env: Env) {
        val instance = setupInstance(env, AwayFacingRule())
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

        val event = firePlace(instance, player, Block.OAK_STAIRS, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.OAK_STAIRS.withProperty("facing", "south"), event.block)
    }

    @Test
    fun `all axis rule uses the clicked face axis`(env: Env) {
        val instance = setupInstance(env, LogRule())
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

        val horizontal = firePlace(instance, player, Block.OAK_LOG, BlockFace.EAST, BlockVec(0, 40, 0))
        assertEquals(Block.OAK_LOG.withProperty("axis", "x"), horizontal.block)
    }

    @Test
    fun `click facing rule uses the clicked face`(env: Env) {
        val instance = setupInstance(env, ClusterRule())
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

        val event = firePlace(instance, player, Block.AMETHYST_CLUSTER, BlockFace.WEST, BlockVec(0, 40, 0))

        assertEquals(
            Block.AMETHYST_CLUSTER.withProperty("facing", "west").withProperty("waterlogged", "false"),
            event.block,
        )
    }

    @Test
    fun `vertical facing rule follows the pitch`(env: Env) {
        val instance = setupInstance(env, EndRodRule())
        val lookingUp = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, -45f))
        val up = firePlace(instance, lookingUp, Block.END_ROD, BlockFace.TOP, BlockVec(0, 40, 0))
        assertEquals(Block.END_ROD.withProperty("facing", "up"), up.block)

        val lookingDown = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 45f))
        val down = firePlace(instance, lookingDown, Block.END_ROD, BlockFace.TOP, BlockVec(0, 40, 1))
        assertEquals(Block.END_ROD.withProperty("facing", "down"), down.block)
    }

    @Test
    fun `nearest facing rule uses the dominant look axis`(env: Env) {
        val instance = setupInstance(env, BarrelRule())
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 180f, 0f))

        val event = firePlace(instance, player, Block.BARREL, BlockFace.TOP, BlockVec(0, 40, 0))

        assertTrue(event.block.getProperty("facing") != null)
    }
}
