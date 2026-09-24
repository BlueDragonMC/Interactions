package com.bluedragonmc.interactions.internal

import com.bluedragonmc.interactions.rules.base.fireInteract
import com.bluedragonmc.interactions.rules.base.firePlace
import com.bluedragonmc.interactions.rules.base.holdBlock
import com.bluedragonmc.interactions.rules.base.setupInstance
import com.bluedragonmc.interactions.rules.base.simulatePlace
import com.bluedragonmc.interactions.rules.directional.LogAxisRule
import com.bluedragonmc.interactions.rules.interaction.FireRule
import com.bluedragonmc.interactions.rules.structural.SlabPlacementRule
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.EventDispatcher
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@EnvTest
class PlacementHandlerTest {

    private fun player(env: Env, instance: Instance) =
        env.createPlayer(instance, Pos(0.5, 40.0, 0.5, 0f, 0f))

    @Test
    fun `blocks replace short grass instead of being placed beside it`(env: Env) {
        val instance = setupInstance(env, LogAxisRule())
        instance.setBlock(0, 40, 0, Block.SHORT_GRASS)

        firePlace(instance, player(env, instance), Block.OAK_LOG, BlockFace.TOP, BlockVec(0, 41, 0))

        assertEquals(Block.OAK_LOG, instance.getBlock(0, 40, 0))
        assertEquals(Block.AIR, instance.getBlock(0, 41, 0))
    }

    @Test
    fun `blocks replace short grass when the adjacent target is occupied`(env: Env) {
        val instance = setupInstance(env, LogAxisRule())
        instance.setBlock(0, 40, 0, Block.SHORT_GRASS)
        instance.setBlock(0, 41, 0, Block.STONE)

        val player = player(env, instance)
        holdBlock(player, Block.OAK_LOG)
        fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.OAK_LOG, instance.getBlock(0, 40, 0))
        assertEquals(Block.STONE, instance.getBlock(0, 41, 0))
    }

    @Test
    fun `breaking a waterlogged block leaves water behind`(env: Env) {
        val instance = setupInstance(env, SlabPlacementRule())
        val block = Block.OAK_SLAB.withProperty("type", "bottom").withProperty("waterlogged", "true")
        instance.setBlock(0, 40, 0, block)

        val event = PlayerBlockBreakEvent(player(env, instance), instance, block, Block.AIR, BlockVec(0, 40, 0), BlockFace.TOP)
        EventDispatcher.call(event)

        assertEquals(Block.WATER, event.resultBlock)
    }

    @Test
    fun `blocks with no rule replace fire instead of being placed beside it`(env: Env) {
        val instance = setupInstance(env, FireRule())
        instance.setBlock(0, 40, 0, Block.FIRE)
        val player = player(env, instance)

        // Minestom resolves the placement above the fire; the library should replace the fire instead.
        firePlace(instance, player, Block.STONE, BlockFace.TOP, BlockVec(0, 41, 0))

        assertEquals(Block.STONE, instance.getBlock(0, 40, 0))
        assertEquals(Block.AIR, instance.getBlock(0, 41, 0))
    }

    @Test
    fun `blocks with no rule replace short grass`(env: Env) {
        val instance = setupInstance(env)
        instance.setBlock(0, 40, 0, Block.SHORT_GRASS)
        val player = player(env, instance)

        firePlace(instance, player, Block.STONE, BlockFace.TOP, BlockVec(0, 41, 0))

        assertEquals(Block.STONE, instance.getBlock(0, 40, 0))
        assertEquals(Block.AIR, instance.getBlock(0, 41, 0))
    }

    @Test
    fun `replaceable clicks are relocated through the real placement listener`(env: Env) {
        val instance = setupInstance(env)
        // Two blocks away from the player so the resolved placement does not collide with them.
        instance.setBlock(0, 40, 2, Block.SHORT_GRASS)

        simulatePlace(player(env, instance), Block.STONE, BlockVec(0, 40, 2), BlockFace.TOP)

        assertEquals(Block.STONE, instance.getBlock(0, 40, 2))
        assertEquals(Block.AIR, instance.getBlock(0, 41, 2))
    }
}
