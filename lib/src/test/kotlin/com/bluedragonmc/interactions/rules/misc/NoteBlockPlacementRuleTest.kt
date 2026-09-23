package com.bluedragonmc.interactions.rules.misc

import com.bluedragonmc.interactions.rules.base.firePlace
import com.bluedragonmc.interactions.rules.base.setupInstance
import com.bluedragonmc.interactions.util.States
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
class NoteBlockPlacementRuleTest {

    private fun player(env: Env, instance: Instance) =
        env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

    @Test
    fun `note block takes its instrument from the block below`(env: Env) {
        val instance = setupInstance(env, NoteBlockPlacementRule())
        instance.setBlock(0, 39, 0, Block.STONE)

        val event = firePlace(instance, player(env, instance), Block.NOTE_BLOCK, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("basedrum", event.block.getProperty(States.INSTRUMENT))
    }

    @Test
    fun `wood below gives the bass instrument`(env: Env) {
        val instance = setupInstance(env, NoteBlockPlacementRule())
        instance.setBlock(0, 39, 0, Block.OAK_PLANKS)

        val event = firePlace(instance, player(env, instance), Block.NOTE_BLOCK, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("bass", event.block.getProperty(States.INSTRUMENT))
    }

    @Test
    fun `a wooden stair below gives the bass instrument`(env: Env) {
        val instance = setupInstance(env, NoteBlockPlacementRule())
        instance.setBlock(0, 39, 0, Block.OAK_STAIRS)

        val event = firePlace(instance, player(env, instance), Block.NOTE_BLOCK, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("bass", event.block.getProperty(States.INSTRUMENT))
    }

    @Test
    fun `a wooden button below stays on the default instrument`(env: Env) {
        val instance = setupInstance(env, NoteBlockPlacementRule())
        instance.setBlock(0, 39, 0, Block.OAK_BUTTON)

        val event = firePlace(instance, player(env, instance), Block.NOTE_BLOCK, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("harp", event.block.getProperty(States.INSTRUMENT))
    }

    @Test
    fun `a colored wool stair below uses the guitar instrument`(env: Env) {
        val instance = setupInstance(env, NoteBlockPlacementRule())
        instance.setBlock(0, 39, 0, Block.WHITE_WOOL_STAIRS)

        val event = firePlace(instance, player(env, instance), Block.NOTE_BLOCK, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("guitar", event.block.getProperty(States.INSTRUMENT))
    }

    @Test
    fun `a waxed oxidized copper block below uses the oxidized trumpet`(env: Env) {
        val instance = setupInstance(env, NoteBlockPlacementRule())
        instance.setBlock(0, 39, 0, Block.WAXED_OXIDIZED_CUT_COPPER)

        val event = firePlace(instance, player(env, instance), Block.NOTE_BLOCK, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("trumpet_oxidized", event.block.getProperty(States.INSTRUMENT))
    }

    @Test
    fun `a mob head above overrides the block below`(env: Env) {
        val instance = setupInstance(env, NoteBlockPlacementRule())
        instance.setBlock(0, 39, 0, Block.STONE)
        instance.setBlock(0, 41, 0, Block.ZOMBIE_HEAD)

        val event = firePlace(instance, player(env, instance), Block.NOTE_BLOCK, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("zombie", event.block.getProperty(States.INSTRUMENT))
    }

    @Test
    fun `changing the block below updates the instrument`(env: Env) {
        val instance = setupInstance(env, NoteBlockPlacementRule())
        instance.setBlock(0, 40, 0, Block.NOTE_BLOCK)

        instance.setBlock(0, 39, 0, Block.GOLD_BLOCK)

        assertEquals("bell", instance.getBlock(0, 40, 0).getProperty(States.INSTRUMENT))
    }
}
