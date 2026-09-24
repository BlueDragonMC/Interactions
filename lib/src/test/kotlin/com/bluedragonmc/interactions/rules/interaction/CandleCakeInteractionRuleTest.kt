package com.bluedragonmc.interactions.rules.interaction

import com.bluedragonmc.interactions.rules.base.fireInteract
import com.bluedragonmc.interactions.rules.base.holdMaterial
import com.bluedragonmc.interactions.rules.base.setupInstance
import com.bluedragonmc.interactions.rules.interaction.misc.CandleCakeInteractionRule
import com.bluedragonmc.interactions.util.States
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.item.Material
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@EnvTest
class CandleCakeInteractionRuleTest {

    @Test
    fun `lighting a candle cake does not place fire on the clicked face`(env: Env) {
        val instance = setupInstance(env, FireStarterRule(), CandleCakeInteractionRule())
        val candleCake = Block.CANDLE_CAKE.withProperty("lit", "false")
        instance.setBlock(0, 40, 0, candleCake)
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))
        holdMaterial(player, Material.FLINT_AND_STEEL)

        val event = fireInteract(instance, player, BlockFace.EAST, BlockVec(0, 40, 0))

        assertTrue(event.isBlockingItemUse)
        assertEquals("true", instance.getBlock(0, 40, 0).getProperty(States.LIT))
        assertEquals(Block.AIR, instance.getBlock(1, 40, 0))
    }
}
