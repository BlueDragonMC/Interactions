package com.bluedragonmc.interactions.rules.interaction

import com.bluedragonmc.interactions.rules.base.*
import com.bluedragonmc.interactions.rules.interaction.sign.SignInteractionRule
import com.bluedragonmc.interactions.rules.wall.SignRule
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.ListBinaryTag
import net.kyori.adventure.nbt.StringBinaryTag
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.event.EventDispatcher
import net.minestom.server.event.player.PlayerEditSignEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.instance.block.SignTextSlot
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.OpenSignEditorPacket
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@EnvTest
class SignInteractionRulesTest {

    private fun signNbt(color: String = "black", glowing: Boolean = false): CompoundBinaryTag =
        CompoundBinaryTag.builder()
            .put(
                "front_text",
                CompoundBinaryTag.builder()
                    .putString("color", color)
                    .putBoolean("has_glowing_text", glowing)
                    .build(),
            )
            .build()

    private fun actor(env: Env, instance: Instance): Player =
        // South of the sign so the standing sign's front text faces the player.
        env.createPlayer(instance, Pos(0.0, 40.0, 3.0, 0f, 0f))

    @Test
    fun `dye colors the front text and is consumed`(env: Env) {
        val instance = setupInstance(env, SignInteractionRule())
        val sign = Block.OAK_SIGN.withProperty("rotation", "0").withNbt(signNbt())
        instance.setBlock(0, 40, 0, sign)
        val player = actor(env, instance)
        holdMaterial(player, Material.RED_DYE)

        val event = fireInteract(instance, player, sign, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertEquals("red", instance.getBlock(0, 40, 0).nbt()!!.getCompound("front_text").getString("color"))
        assertTrue(event.isBlockingItemUse)
        assertEquals(Material.AIR, player.itemInMainHand.material())
    }

    @Test
    fun `glow ink sac enables glowing text`(env: Env) {
        val instance = setupInstance(env, SignInteractionRule())
        val sign = Block.OAK_SIGN.withProperty("rotation", "0").withNbt(signNbt())
        instance.setBlock(0, 40, 0, sign)
        val player = actor(env, instance)
        holdMaterial(player, Material.GLOW_INK_SAC)

        fireInteract(instance, player, sign, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertTrue(instance.getBlock(0, 40, 0).nbt()!!.getCompound("front_text").getBoolean("has_glowing_text"))
    }

    @Test
    fun `honeycomb waxes the sign`(env: Env) {
        val instance = setupInstance(env, SignInteractionRule())
        val sign = Block.OAK_SIGN.withProperty("rotation", "0").withNbt(signNbt())
        instance.setBlock(0, 40, 0, sign)
        val player = actor(env, instance)
        holdMaterial(player, Material.HONEYCOMB)

        val event = fireInteract(instance, player, sign, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertTrue(instance.getBlock(0, 40, 0).nbt()!!.getBoolean("is_waxed"))
        assertTrue(event.isBlockingItemUse)
    }

    @Test
    fun `empty hand opens the sign editor`(env: Env) {
        val instance = setupInstance(env, SignInteractionRule())
        val sign = Block.OAK_SIGN.withProperty("rotation", "0").withNbt(signNbt())
        instance.setBlock(0, 40, 0, sign)
        val connection = env.createConnection()
        val player = connection.connect(instance, Pos(0.0, 40.0, 3.0, 0f, 0f))
        val editors = connection.trackIncoming(OpenSignEditorPacket::class.java)

        fireInteract(instance, player, sign, BlockFace.NORTH, BlockVec(0, 40, 0))

        editors.assertSingle()
    }

    @Test
    fun `placing a sign opens the front editor`(env: Env) {
        val instance = setupInstance(env, SignRule(), SignInteractionRule())
        instance.setBlock(0, 39, 0, Block.STONE)
        val connection = env.createConnection()
        val player = connection.connect(instance, Pos(0.5, 40.0, 0.5, 0f, 0f))
        val editors = connection.trackIncoming(OpenSignEditorPacket::class.java)

        simulatePlace(player, Block.OAK_SIGN, BlockVec(0, 39, 0), BlockFace.TOP)

        editors.assertSingle()
    }

    @Test
    fun `clicking a sign with another sign opens the editor`(env: Env) {
        val instance = setupInstance(env, SignRule(), SignInteractionRule())
        val sign = Block.OAK_SIGN.withProperty("rotation", "0")
        instance.setBlock(0, 40, 0, sign)
        val connection = env.createConnection()
        val player = connection.connect(instance, Pos(0.0, 40.0, 3.0, 0f, 0f))
        holdBlock(player, Block.OAK_SIGN)
        val editors = connection.trackIncoming(OpenSignEditorPacket::class.java)

        val event = fireInteract(instance, player, sign, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertTrue(event.isBlockingItemUse)
        editors.assertSingle()
    }

    @Test
    fun `glow ink sac keeps the sign text`(env: Env) {
        val instance = setupInstance(env, SignInteractionRule())
        val front = CompoundBinaryTag.builder()
            .putString("color", "black")
            .putBoolean("has_glowing_text", false)
            .put("messages", ListBinaryTag.from(listOf(StringBinaryTag.stringBinaryTag("hello"))))
            .build()
        val sign = Block.OAK_SIGN.withProperty("rotation", "0")
            .withNbt(CompoundBinaryTag.builder().put("front_text", front).build())
        instance.setBlock(0, 40, 0, sign)
        val player = actor(env, instance)
        holdMaterial(player, Material.GLOW_INK_SAC)

        fireInteract(instance, player, sign, BlockFace.NORTH, BlockVec(0, 40, 0))

        val frontText = instance.getBlock(0, 40, 0).nbt()!!.getCompound("front_text")
        assertTrue(frontText.getBoolean("has_glowing_text"))
        assertEquals(1, frontText.getList("messages").size())
    }

    @Test
    fun `editing a sign persists its text`(env: Env) {
        val instance = setupInstance(env, SignInteractionRule())
        val sign = Block.OAK_SIGN.withProperty("rotation", "0")
        instance.setBlock(0, 40, 0, sign)
        val player = actor(env, instance)

        EventDispatcher.call(
            PlayerEditSignEvent(
                player,
                instance,
                sign,
                BlockVec(0, 40, 0),
                listOf("a", "b", "c", "d"),
                SignTextSlot.FRONT,
            ),
        )

        val frontText = instance.getBlock(0, 40, 0).nbt()!!.getCompound("front_text")
        assertEquals(4, frontText.getList("messages").size())
    }
}

