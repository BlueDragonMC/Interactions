package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.event.BlockPlacementEvent
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.EventDispatcher
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@EnvTest
class MultiBlockPlacementRuleTest {

    private class SunflowerRule : MultiBlockPlacementRule(BlockSet.from(Block.SUNFLOWER)) {
        override fun primaryState(event: BlockPlacementEvent): Block =
            event.block.withProperty("half", "lower")

        override fun secondaryPosition(primary: Block, event: BlockPlacementEvent): BlockVec =
            event.blockPosition.relative(BlockFace.TOP)

        override fun secondaryState(primary: Block, event: BlockPlacementEvent): Block =
            primary.withProperty("half", "upper")

        override fun pairPosition(broken: Block, position: BlockVec): BlockVec =
            if (broken.getProperty("half") == "lower") position.relative(BlockFace.TOP)
            else position.relative(BlockFace.BOTTOM)
    }

    @Test
    fun `both halves are placed`(env: Env) {
        val instance = setupInstance(env, SunflowerRule())
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

        firePlace(instance, player, Block.SUNFLOWER, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.SUNFLOWER.withProperty("half", "lower"), instance.getBlock(0, 40, 0))
        assertEquals(Block.SUNFLOWER.withProperty("half", "upper"), instance.getBlock(0, 41, 0))
    }

    @Test
    fun `placement is canceled when the second position is blocked`(env: Env) {
        val instance = setupInstance(env, SunflowerRule())
        instance.setBlock(0, 41, 0, Block.STONE)
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

        val event = firePlace(instance, player, Block.SUNFLOWER, BlockFace.TOP, BlockVec(0, 40, 0))

        kotlin.test.assertTrue(event.isCancelled)
    }

    @Test
    fun `breaking one half removes the other`(env: Env) {
        val instance = setupInstance(env, SunflowerRule())
        val lower = Block.SUNFLOWER.withProperty("half", "lower")
        val upper = Block.SUNFLOWER.withProperty("half", "upper")
        instance.setBlock(0, 40, 0, lower)
        instance.setBlock(0, 41, 0, upper)
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

        EventDispatcher.call(
            PlayerBlockBreakEvent(
                player, instance, lower, Block.AIR, BlockVec(0, 40, 0), BlockFace.TOP,
            ),
        )

        assertEquals(Block.AIR, instance.getBlock(0, 41, 0))
    }
}
