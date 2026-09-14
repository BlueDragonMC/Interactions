package com.bluedragonmc.interactions.rules.wall

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
class WallMountedRulesTest {

    private fun player(env: Env, instance: Instance, yaw: Float = 0f, pitch: Float = 0f) =
        env.createPlayer(instance, Pos(0.0, 40.0, 0.0, yaw, pitch))

    @Test
    fun `torch item mounts on a wall as a wall torch`(env: Env) {
        val instance = setupInstance(env, TorchRule())
        instance.setBlock(0, 40, 1, Block.STONE)

        val event = firePlace(instance, player(env, instance), Block.TORCH, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertFalse(event.isCancelled)
        assertEquals(Block.WALL_TORCH.withProperty("facing", "north"), event.block)
    }

    @Test
    fun `torch item on top of a block stays a standing torch`(env: Env) {
        val instance = setupInstance(env, TorchRule())
        instance.setBlock(0, 39, 0, Block.STONE)

        val event = firePlace(
            instance,
            player(env, instance, pitch = 90f),
            Block.TORCH,
            BlockFace.TOP,
            BlockVec(0, 40, 0),
        )

        assertEquals(Block.TORCH.key(), event.block.key())
    }

    @Test
    fun `sign item mounts on a wall as a wall sign`(env: Env) {
        val instance = setupInstance(env, SignRule())
        instance.setBlock(0, 40, 1, Block.STONE)

        val event = firePlace(instance, player(env, instance), Block.OAK_SIGN, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertFalse(event.isCancelled)
        assertEquals(
            Block.OAK_WALL_SIGN.withProperty("facing", "north").withProperty("waterlogged", "false"),
            event.block,
        )
    }

    @Test
    fun `sign item on top of a block stays a standing sign`(env: Env) {
        val instance = setupInstance(env, SignRule())
        instance.setBlock(0, 39, 0, Block.STONE)

        val event = firePlace(
            instance,
            player(env, instance, pitch = 90f),
            Block.OAK_SIGN,
            BlockFace.TOP,
            BlockVec(0, 40, 0),
        )

        assertFalse(event.isCancelled)
        assertEquals(Block.OAK_SIGN.withProperty("rotation", "8"), event.block)
    }

    @Test
    fun `banner item mounts on a wall as a wall banner`(env: Env) {
        val instance = setupInstance(env, BannerRule())
        instance.setBlock(0, 40, 1, Block.STONE)

        val event = firePlace(
            instance,
            player(env, instance),
            Block.WHITE_BANNER,
            BlockFace.NORTH,
            BlockVec(0, 40, 0),
        )

        assertEquals("north", event.block.getProperty("facing"))
        assertEquals(Block.WHITE_WALL_BANNER.key(), event.block.key())
    }

    @Test
    fun `skull item mounts on a wall as a wall skull`(env: Env) {
        val instance = setupInstance(env, SkullRule())
        instance.setBlock(0, 40, 1, Block.STONE)

        val event = firePlace(
            instance,
            player(env, instance),
            Block.SKELETON_SKULL,
            BlockFace.NORTH,
            BlockVec(0, 40, 0),
        )

        assertEquals(Block.SKELETON_WALL_SKULL.key(), event.block.key())
    }

    @Test
    fun `lever mounts on the floor when looking down`(env: Env) {
        val instance = setupInstance(env, LeverRule())
        instance.setBlock(0, 39, 0, Block.STONE)

        val event = firePlace(instance, player(env, instance, pitch = 90f), Block.LEVER, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("floor", event.block.getProperty("face"))
    }

    @Test
    fun `lantern hangs from a block above`(env: Env) {
        val instance = setupInstance(env, LanternRule())
        instance.setBlock(0, 41, 0, Block.STONE)

        val event = firePlace(instance, player(env, instance, pitch = -90f), Block.LANTERN, BlockFace.BOTTOM, BlockVec(0, 40, 0))

        assertFalse(event.isCancelled)
        assertEquals("true", event.block.getProperty("hanging"))
    }

    @Test
    fun `ladder mounts on the wall it is clicked against`(env: Env) {
        val instance = setupInstance(env, LadderRule())
        instance.setBlock(0, 40, 1, Block.STONE)

        val event = firePlace(instance, player(env, instance), Block.LADDER, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertEquals(
            Block.LADDER.withProperty("facing", "north").withProperty("waterlogged", "false"),
            event.block,
        )
    }
}
