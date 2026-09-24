package com.bluedragonmc.interactions.rules.interaction

import com.bluedragonmc.interactions.rules.base.fireInteract
import com.bluedragonmc.interactions.rules.base.holdBlock
import com.bluedragonmc.interactions.rules.base.setupInstance
import com.bluedragonmc.interactions.rules.interaction.misc.ConsumeInteractionRule
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

@EnvTest
class ConsumeInteractionRuleTest {

    @Test
    fun `clicking a workstation consumes the interaction`(env: Env) {
        val instance = setupInstance(env, ConsumeInteractionRule())
        instance.setBlock(0, 40, 0, Block.CRAFTING_TABLE)
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))
        holdBlock(player, Block.STONE)

        val event = fireInteract(instance, player, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertTrue(event.isBlockingItemUse)
    }

    @Test
    fun `sneaking on a workstation allows placing a block`(env: Env) {
        val instance = setupInstance(env, ConsumeInteractionRule())
        instance.setBlock(0, 40, 0, Block.CRAFTING_TABLE)
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))
        player.isSneaking = true
        holdBlock(player, Block.STONE)

        val event = fireInteract(instance, player, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertFalse(event.isBlockingItemUse)
    }
}