package com.bluedragonmc.interactions.rules.interaction

import com.bluedragonmc.interactions.rules.base.fireInteract
import com.bluedragonmc.interactions.rules.base.holdMaterial
import com.bluedragonmc.interactions.rules.base.setupInstance
import com.bluedragonmc.interactions.rules.base.trackSounds
import com.bluedragonmc.interactions.rules.interaction.connecting.FenceGateInteractionRule
import com.bluedragonmc.interactions.rules.interaction.misc.CandleInteractionRule
import com.bluedragonmc.interactions.rules.interaction.misc.NoteBlockInteractionRule
import com.bluedragonmc.interactions.rules.interaction.redstone.ButtonInteractionRule
import com.bluedragonmc.interactions.rules.interaction.redstone.ComparatorInteractionRule
import com.bluedragonmc.interactions.rules.interaction.redstone.DaylightDetectorInteractionRule
import com.bluedragonmc.interactions.rules.interaction.redstone.RepeaterInteractionRule
import com.bluedragonmc.interactions.rules.interaction.structural.DoorInteractionRule
import com.bluedragonmc.interactions.rules.interaction.structural.TrapdoorInteractionRule
import com.bluedragonmc.interactions.rules.interaction.wall.LeverInteractionRule
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.item.Material
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@EnvTest
class BlockInteractionRulesTest {

    private fun actor(env: Env, instance: Instance, yaw: Float = 0f): Player =
        env.createPlayer(instance, Pos(0.0, 40.0, 0.0, yaw, 0f))

    // --- Levers -------------------------------------------------------------------------------

    @Test
    fun `lever toggles and clicks`(env: Env) {
        val instance = setupInstance(env, LeverInteractionRule())
        val lever = Block.LEVER.withProperty("powered", "false")
        instance.setBlock(0, 40, 0, lever)
        val player = actor(env, instance)
        val sounds = trackSounds(env, instance, Pos(0.0, 40.0, 10.0))

        val event = fireInteract(instance, player, lever, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertEquals("true", instance.getBlock(0, 40, 0).getProperty("powered"))
        assertTrue(event.isBlockingItemUse)
        sounds.assertSingle { assertEquals("block.lever.click", it.soundEvent().key().value()) }
    }

    @Test
    fun `sneaking on a lever leaves it alone`(env: Env) {
        val instance = setupInstance(env, LeverInteractionRule())
        val lever = Block.LEVER.withProperty("powered", "false")
        instance.setBlock(0, 40, 0, lever)
        val player = actor(env, instance)
        player.setSneaking(true)
        holdMaterial(player, Material.STONE)

        val event = fireInteract(instance, player, lever, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertEquals("false", instance.getBlock(0, 40, 0).getProperty("powered"))
        assertFalse(event.isBlockingItemUse)
    }

    // --- Buttons ------------------------------------------------------------------------------

    @Test
    fun `stone button presses and releases after twenty ticks`(env: Env) {
        val instance = setupInstance(env, ButtonInteractionRule())
        val button = Block.STONE_BUTTON.withProperty("powered", "false")
        instance.setBlock(0, 40, 0, button)
        val player = actor(env, instance)
        val sounds = trackSounds(env, instance, Pos(0.0, 40.0, 10.0))

        val event = fireInteract(instance, player, button, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertEquals("true", instance.getBlock(0, 40, 0).getProperty("powered"))
        assertTrue(event.isBlockingItemUse)
        sounds.assertSingle { assertEquals("block.stone_button.click_on", it.soundEvent().key().value()) }

        repeat(25) { env.tick() }
        assertEquals("false", instance.getBlock(0, 40, 0).getProperty("powered"))
    }

    // --- Doors and trapdoors ------------------------------------------------------------------

    @Test
    fun `door opens both halves with a sound`(env: Env) {
        val instance = setupInstance(env, DoorInteractionRule())
        val lower = Block.OAK_DOOR.withProperty("half", "lower").withProperty("open", "false")
        val upper = Block.OAK_DOOR.withProperty("half", "upper").withProperty("open", "false")
        instance.setBlock(0, 40, 0, lower)
        instance.setBlock(0, 41, 0, upper)
        val player = actor(env, instance)
        val sounds = trackSounds(env, instance, Pos(0.0, 40.0, 10.0))

        val event = fireInteract(instance, player, lower, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertEquals("true", instance.getBlock(0, 40, 0).getProperty("open"))
        assertEquals("true", instance.getBlock(0, 41, 0).getProperty("open"))
        assertTrue(event.isBlockingItemUse)
        sounds.assertSingle { assertEquals("block.wooden_door.open", it.soundEvent().key().value()) }
    }

    @Test
    fun `iron door ignores hand interaction`(env: Env) {
        val instance = setupInstance(env, DoorInteractionRule())
        val door = Block.IRON_DOOR.withProperty("half", "lower").withProperty("open", "false")
        instance.setBlock(0, 40, 0, door)
        val player = actor(env, instance)

        val event = fireInteract(instance, player, door, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertEquals("false", instance.getBlock(0, 40, 0).getProperty("open"))
        assertFalse(event.isBlockingItemUse)
    }

    @Test
    fun `trapdoor opens with a sound`(env: Env) {
        val instance = setupInstance(env, TrapdoorInteractionRule())
        val trapdoor = Block.OAK_TRAPDOOR.withProperty("open", "false")
        instance.setBlock(0, 40, 0, trapdoor)
        val player = actor(env, instance)
        val sounds = trackSounds(env, instance, Pos(0.0, 40.0, 10.0))

        val event = fireInteract(instance, player, trapdoor, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertEquals("true", instance.getBlock(0, 40, 0).getProperty("open"))
        assertTrue(event.isBlockingItemUse)
        sounds.assertSingle { assertEquals("block.wooden_trapdoor.open", it.soundEvent().key().value()) }
    }

    // --- Fence gates --------------------------------------------------------------------------

    @Test
    fun `fence gate opens, faces the player, and clicks`(env: Env) {
        val instance = setupInstance(env, FenceGateInteractionRule())
        val gate = Block.OAK_FENCE_GATE.withProperty("open", "false").withProperty("facing", "north")
        instance.setBlock(0, 40, 0, gate)
        val player = actor(env, instance) // yaw 0 faces south
        val sounds = trackSounds(env, instance, Pos(0.0, 40.0, 10.0))

        val event = fireInteract(instance, player, gate, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertEquals("true", instance.getBlock(0, 40, 0).getProperty("open"))
        assertEquals("south", instance.getBlock(0, 40, 0).getProperty("facing"))
        assertTrue(event.isBlockingItemUse)
        sounds.assertSingle { assertEquals("block.fence_gate.open", it.soundEvent().key().value()) }
    }

    // --- Candles ------------------------------------------------------------------------------

    @Test
    fun `flint and steel lights a candle`(env: Env) {
        val instance = setupInstance(env, CandleInteractionRule())
        val candle = Block.CANDLE.withProperty("lit", "false")
        instance.setBlock(0, 40, 0, candle)
        val player = actor(env, instance)
        holdMaterial(player, Material.FLINT_AND_STEEL)

        val event = fireInteract(instance, player, candle, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("true", instance.getBlock(0, 40, 0).getProperty("lit"))
        assertTrue(event.isBlockingItemUse)
    }

    @Test
    fun `empty hand extinguishes a lit candle`(env: Env) {
        val instance = setupInstance(env, CandleInteractionRule())
        val candle = Block.CANDLE.withProperty("lit", "true")
        instance.setBlock(0, 40, 0, candle)
        val player = actor(env, instance)
        val sounds = trackSounds(env, instance, Pos(0.0, 40.0, 10.0))

        val event = fireInteract(instance, player, candle, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("false", instance.getBlock(0, 40, 0).getProperty("lit"))
        assertTrue(event.isBlockingItemUse)
        sounds.assertSingle { assertEquals("block.candle.extinguish", it.soundEvent().key().value()) }
    }

    @Test
    fun `waterlogged candles cannot be lit`(env: Env) {
        val instance = setupInstance(env, CandleInteractionRule())
        val candle = Block.CANDLE.withProperty("lit", "false").withProperty("waterlogged", "true")
        instance.setBlock(0, 40, 0, candle)
        val player = actor(env, instance)
        holdMaterial(player, Material.FLINT_AND_STEEL)

        val event = fireInteract(instance, player, candle, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("false", instance.getBlock(0, 40, 0).getProperty("lit"))
        assertFalse(event.isBlockingItemUse)
    }

    // --- Redstone components ------------------------------------------------------------------

    @Test
    fun `repeater cycles its delay`(env: Env) {
        val instance = setupInstance(env, RepeaterInteractionRule())
        val repeater = Block.REPEATER.withProperty("delay", "4")
        instance.setBlock(0, 40, 0, repeater)
        val player = actor(env, instance)

        fireInteract(instance, player, repeater, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertEquals("1", instance.getBlock(0, 40, 0).getProperty("delay"))
    }

    @Test
    fun `comparator toggles mode and clicks`(env: Env) {
        val instance = setupInstance(env, ComparatorInteractionRule())
        val comparator = Block.COMPARATOR.withProperty("mode", "compare")
        instance.setBlock(0, 40, 0, comparator)
        val player = actor(env, instance)
        val sounds = trackSounds(env, instance, Pos(0.0, 40.0, 10.0))

        fireInteract(instance, player, comparator, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertEquals("subtract", instance.getBlock(0, 40, 0).getProperty("mode"))
        sounds.assertSingle { assertEquals("block.comparator.click", it.soundEvent().key().value()) }
    }

    @Test
    fun `daylight detector toggles inverted`(env: Env) {
        val instance = setupInstance(env, DaylightDetectorInteractionRule())
        val detector = Block.DAYLIGHT_DETECTOR.withProperty("inverted", "false")
        instance.setBlock(0, 40, 0, detector)
        val player = actor(env, instance)

        fireInteract(instance, player, detector, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("true", instance.getBlock(0, 40, 0).getProperty("inverted"))
    }

    // --- Note blocks --------------------------------------------------------------------------

    @Test
    fun `note block increments its note and plays the instrument`(env: Env) {
        val instance = setupInstance(env, NoteBlockInteractionRule())
        val noteBlock = Block.NOTE_BLOCK.withProperty("note", "0").withProperty("instrument", "harp")
        instance.setBlock(0, 40, 0, noteBlock)
        val player = actor(env, instance)
        val sounds = trackSounds(env, instance, Pos(0.0, 40.0, 10.0))

        fireInteract(instance, player, noteBlock, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertEquals("1", instance.getBlock(0, 40, 0).getProperty("note"))
        sounds.assertSingle { assertEquals("block.note_block.harp", it.soundEvent().key().value()) }
    }
}
