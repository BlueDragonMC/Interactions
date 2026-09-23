package com.bluedragonmc.interactions.rules

import com.bluedragonmc.interactions.rules.base.firePlace
import com.bluedragonmc.interactions.rules.base.setupInstance
import com.bluedragonmc.interactions.rules.misc.FarmlandRule
import com.bluedragonmc.interactions.rules.misc.SnowyRule
import com.bluedragonmc.interactions.rules.redstone.RedstoneWireRule
import com.bluedragonmc.interactions.rules.redstone.TripwireRule
import com.bluedragonmc.interactions.rules.wall.BellRule
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
class PlacementParityRulesTest {

    private fun player(env: Env, instance: Instance) =
        env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

    @Test
    fun `redstone wire connects and starts unpowered`(env: Env) {
        val instance = setupInstance(env, RedstoneWireRule())

        val event = firePlace(instance, player(env, instance), Block.REDSTONE_WIRE, BlockFace.TOP, BlockVec(0, 40, 0))

        // Redstone dust starts out in a cross shape where every side connects outwards.
        assertEquals("side", event.block.getProperty("north"))
        assertEquals("side", event.block.getProperty("east"))
        assertEquals("0", event.block.getProperty("power"))
    }

    @Test
    fun `tripwire starts disconnected`(env: Env) {
        val instance = setupInstance(env, TripwireRule())

        val event = firePlace(instance, player(env, instance), Block.TRIPWIRE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("false", event.block.getProperty("north"))
        assertEquals("false", event.block.getProperty("west"))
    }

    @Test
    fun `bell placed on the ground uses the floor attachment`(env: Env) {
        val instance = setupInstance(env, BellRule())

        val event = firePlace(instance, player(env, instance), Block.BELL, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("floor", event.block.getProperty("attachment"))
    }

    @Test
    fun `farmland becomes dirt under a solid block`(env: Env) {
        val instance = setupInstance(env, FarmlandRule())
        instance.setBlock(0, 41, 0, Block.STONE)

        val event = firePlace(instance, player(env, instance), Block.FARMLAND, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.DIRT, event.block)
    }

    @Test
    fun `grass becomes snowy under a snow layer`(env: Env) {
        val instance = setupInstance(env, SnowyRule())
        instance.setBlock(0, 41, 0, Block.SNOW)

        val event = firePlace(instance, player(env, instance), Block.GRASS_BLOCK, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("true", event.block.getProperty("snowy"))
    }
}
