package com.bluedragonmc.interactions.rules

import com.bluedragonmc.interactions.PlacementRuleRegistry
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.PlayerHand
import net.minestom.server.event.EventDispatcher
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@EnvTest
class ButtonPlacementRuleTest {

    @Test
    fun `button mounts on the block the player is looking at`(env: Env) {
        val registry = PlacementRuleRegistry()
        registry.register(ButtonPlacementRule())
        MinecraftServer.getGlobalEventHandler().addChild(registry.eventNode())

        val instance = env.createFlatInstance()
        instance.loadChunk(0, 0).join()
        // Player looks south (yaw 0), so the button faces north and mounts on the block to its south.
        val player = env.createPlayer(instance, Pos(0.0, 42.0, 0.0, 0f, 0f))
        player.setView(0f, 0f)
        instance.setBlock(0, 42, 1, Block.STONE)

        val event = PlayerBlockPlaceEvent(
            player,
            instance,
            Block.OAK_BUTTON,
            BlockFace.TOP,
            BlockVec(0, 42, 0),
            Vec(0.5, 0.5, 0.5),
            PlayerHand.MAIN,
        )
        EventDispatcher.call(event)

        assertFalse(event.isCancelled)
        assertEquals(
            Block.OAK_BUTTON.withProperty("facing", "north").withProperty("face", "wall"),
            event.block,
        )
    }

    @Test
    fun `button is not placed without a supporting block`(env: Env) {
        val registry = PlacementRuleRegistry()
        registry.register(ButtonPlacementRule())
        MinecraftServer.getGlobalEventHandler().addChild(registry.eventNode())

        val instance = env.createFlatInstance()
        instance.loadChunk(0, 0).join()
        val player = env.createPlayer(instance, Pos(0.0, 42.0, 0.0, 0f, 0f))
        player.setView(0f, 0f)

        val event = PlayerBlockPlaceEvent(
            player,
            instance,
            Block.OAK_BUTTON,
            BlockFace.TOP,
            BlockVec(0, 42, 0),
            Vec(0.5, 0.5, 0.5),
            PlayerHand.MAIN,
        )
        EventDispatcher.call(event)

        assertTrue(event.isCancelled)
    }
}
