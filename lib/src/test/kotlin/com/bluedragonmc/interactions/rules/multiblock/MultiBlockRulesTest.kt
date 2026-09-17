package com.bluedragonmc.interactions.rules.multiblock

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

@EnvTest
class MultiBlockRulesTest {

    private fun player(env: Env, instance: Instance, yaw: Float = 0f) =
        env.createPlayer(instance, Pos(0.0, 40.0, 0.0, yaw, 0f))

    @Test
    fun `door places an upper and lower half`(env: Env) {
        val instance = setupInstance(env, DoorRule())
        instance.setBlock(0, 39, 0, Block.STONE)

        firePlace(instance, player(env, instance), Block.OAK_DOOR, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("lower", instance.getBlock(0, 40, 0).getProperty("half"))
        assertEquals("upper", instance.getBlock(0, 41, 0).getProperty("half"))
        assertEquals("south", instance.getBlock(0, 40, 0).getProperty("facing"))
    }

    @Test
    fun `bed places a foot and a head`(env: Env) {
        val instance = setupInstance(env, BedRule())
        instance.setBlock(0, 39, 0, Block.STONE)

        firePlace(instance, player(env, instance), Block.RED_BED, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("foot", instance.getBlock(0, 40, 0).getProperty("part"))
        assertEquals("head", instance.getBlock(0, 40, 1).getProperty("part"))
    }

    @Test
    fun `tall plant places both halves`(env: Env) {
        val instance = setupInstance(env, TallPlantRule())
        instance.setBlock(0, 39, 0, Block.GRASS_BLOCK)

        firePlace(instance, player(env, instance), Block.SUNFLOWER, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("lower", instance.getBlock(0, 40, 0).getProperty("half"))
        assertEquals("upper", instance.getBlock(0, 41, 0).getProperty("half"))
    }

    @Test
    fun `small dripleaf places both halves`(env: Env) {
        val instance = setupInstance(env, SmallDripleafRule())
        instance.setBlock(0, 39, 0, Block.CLAY)

        firePlace(instance, player(env, instance), Block.SMALL_DRIPLEAF, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("lower", instance.getBlock(0, 40, 0).getProperty("half"))
        assertEquals("upper", instance.getBlock(0, 41, 0).getProperty("half"))
    }

    @Test
    fun `door hinge is right when the right side is blocked`(env: Env) {
        val instance = setupInstance(env, DoorRule())
        instance.setBlock(1, 40, 0, Block.STONE)

        // yaw 180 -> facing north, whose clockwise (right) side is east.
        firePlace(instance, player(env, instance, yaw = 180f), Block.OAK_DOOR, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("right", instance.getBlock(0, 40, 0).getProperty("hinge"))
    }

    @Test
    fun `door hinge is left when the left side is blocked`(env: Env) {
        val instance = setupInstance(env, DoorRule())
        instance.setBlock(1, 40, 0, Block.STONE)

        // yaw 0 -> facing south, whose counter-clockwise (left) side is east.
        firePlace(instance, player(env, instance), Block.OAK_DOOR, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("left", instance.getBlock(0, 40, 0).getProperty("hinge"))
    }
}
