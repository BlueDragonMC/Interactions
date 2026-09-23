package com.bluedragonmc.interactions.rules.interaction

import com.bluedragonmc.interactions.rules.base.fireInteract
import com.bluedragonmc.interactions.rules.base.setupInstance
import com.bluedragonmc.interactions.rules.interaction.wall.BellInteractionRule
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.network.packet.server.play.BlockActionPacket
import net.minestom.server.network.packet.server.play.SoundEffectPacket
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@EnvTest
class BellInteractionRuleTest {

    private fun bell(attachment: String, facing: String = "north") =
        Block.BELL.withProperty("attachment", attachment).withProperty("facing", facing)

    @Test
    fun `ringing a ceiling bell plays the sound and sends the swing event`(env: Env) {
        val instance = setupInstance(env, BellInteractionRule())
        val bell = bell("ceiling")
        instance.setBlock(0, 40, 0, bell)
        val connection = env.createConnection()
        val player = connection.connect(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))
        val sounds = connection.trackIncoming(SoundEffectPacket::class.java)
        val actions = connection.trackIncoming(BlockActionPacket::class.java)

        val event = fireInteract(instance, player, bell, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertTrue(event.isBlockingItemUse)
        sounds.assertSingle { assertEquals("block.bell.use", it.soundEvent().key().value()) }
        actions.assertSingle { assertEquals(1, it.actionId().toInt()) }
    }

    @Test
    fun `a floor bell rings when hit along its facing axis`(env: Env) {
        val instance = setupInstance(env, BellInteractionRule())
        val bell = bell("floor")
        instance.setBlock(0, 40, 0, bell)
        val connection = env.createConnection()
        val player = connection.connect(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))
        val actions = connection.trackIncoming(BlockActionPacket::class.java)

        val event = fireInteract(instance, player, bell, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertTrue(event.isBlockingItemUse)
        actions.assertSingle()
    }

    @Test
    fun `a floor bell ignores a perpendicular face`(env: Env) {
        val instance = setupInstance(env, BellInteractionRule())
        val bell = bell("floor")
        instance.setBlock(0, 40, 0, bell)
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

        val event = fireInteract(instance, player, bell, BlockFace.EAST, BlockVec(0, 40, 0))

        assertFalse(event.isBlockingItemUse)
    }

    @Test
    fun `a floor bell ignores a hit near the top`(env: Env) {
        val instance = setupInstance(env, BellInteractionRule())
        val bell = bell("floor")
        instance.setBlock(0, 40, 0, bell)
        val player = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

        val event = fireInteract(
            instance,
            player,
            bell,
            BlockFace.NORTH,
            BlockVec(0, 40, 0),
            cursor = Vec(0.5, 0.9, 0.5),
        )

        assertFalse(event.isBlockingItemUse)
    }
}
