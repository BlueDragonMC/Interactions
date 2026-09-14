package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
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
class GrowingPlantPlacementRuleTest {

    private class KelpRule :
        GrowingPlantPlacementRule(BlockSet.from(Block.KELP, Block.KELP_PLANT), Direction.UP) {
        override fun headBlock(): Block = Block.KELP
        override fun bodyBlock(): Block = Block.KELP_PLANT
        override fun requiresWater(): Boolean = true
    }

    @Test
    fun `plant is placed as a head when in water on a support`(env: Env) {
        val instance = setupInstance(env, KelpRule())
        instance.setBlock(0, 40, 0, Block.WATER)
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

        val event = firePlace(instance, player, Block.KELP, BlockFace.TOP, BlockVec(0, 40, 0))

        kotlin.test.assertFalse(event.isCancelled)
        assertEquals(Block.KELP.key(), event.block.key())
    }

    @Test
    fun `plant is not placed without water`(env: Env) {
        val instance = setupInstance(env, KelpRule())
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

        val event = firePlace(instance, player, Block.KELP, BlockFace.TOP, BlockVec(0, 40, 0))

        kotlin.test.assertTrue(event.isCancelled)
    }

    @Test
    fun `head becomes a body when a segment is placed ahead`(env: Env) {
        val instance = setupInstance(env, KelpRule())
        instance.setBlock(0, 40, 0, Block.KELP)

        instance.setBlock(0, 41, 0, Block.KELP)

        assertEquals(Block.KELP_PLANT, instance.getBlock(0, 40, 0))
    }

    @Test
    fun `plant is destroyed when its support is removed`(env: Env) {
        val instance = setupInstance(env, KelpRule())
        instance.setBlock(0, 40, 0, Block.KELP)

        instance.setBlock(0, 39, 0, Block.AIR)

        assertEquals(Block.AIR, instance.getBlock(0, 40, 0))
    }
}
