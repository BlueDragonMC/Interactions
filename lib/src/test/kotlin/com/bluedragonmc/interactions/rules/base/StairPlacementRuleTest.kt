package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@EnvTest
class StairPlacementRuleTest {

    private class OakStairsRule : StairPlacementRule(BlockSet.from(Block.OAK_STAIRS))

    @Test
    fun `stairs face the player and default to a bottom straight stair`(env: Env) {
        val instance = setupInstance(env, OakStairsRule())
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

        val event = firePlace(instance, player, Block.OAK_STAIRS, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(
            Block.OAK_STAIRS
                .withProperty("facing", "south")
                .withProperty("half", "bottom")
                .withProperty("shape", "straight")
                .withProperty("waterlogged", "false"),
            event.block,
        )
    }

    @Test
    fun `stairs placed high on a side face are top stairs`(env: Env) {
        val instance = setupInstance(env, OakStairsRule())
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

        val event = firePlace(
            instance,
            player,
            Block.OAK_STAIRS,
            BlockFace.NORTH,
            BlockVec(0, 40, 0),
            Vec(0.5, 0.8, 0.5),
        )

        assertEquals("top", event.block.getProperty("half"))
    }

    @Test
    fun `neighbor updates recompute the stair shape`(env: Env) {
        val instance = setupInstance(env, OakStairsRule())

        // A faces north; B sits in A's front (south) and faces east, turning A into an inner-right corner.
        instance.setBlock(
            0, 40, 0,
            Block.OAK_STAIRS.withProperty("facing", "north").withProperty("half", "bottom").withProperty("shape", "straight"),
        )
        instance.setBlock(
            0, 40, 1,
            Block.OAK_STAIRS.withProperty("facing", "east").withProperty("half", "bottom").withProperty("shape", "straight"),
        )

        assertEquals("inner_right", instance.getBlock(0, 40, 0).getProperty("shape"))
        assertEquals("straight", instance.getBlock(0, 40, 1).getProperty("shape"))
    }
}
