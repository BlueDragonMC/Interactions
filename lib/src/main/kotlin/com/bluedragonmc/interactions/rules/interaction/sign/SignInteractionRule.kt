package com.bluedragonmc.interactions.rules.interaction.sign

import com.bluedragonmc.interactions.BlockInteractionRule
import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.plus
import com.bluedragonmc.interactions.util.*
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.ListBinaryTag
import net.kyori.adventure.nbt.StringBinaryTag
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.player.PlayerEditSignEvent
import net.minestom.server.instance.block.SignTextSlot
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.OpenSignEditorPacket
import net.minestom.server.network.packet.server.play.WorldEventPacket
import net.minestom.server.sound.SoundEvent
import net.minestom.server.worldevent.WorldEvent

/**
 * Sign interactions: open the editor, dye the text, toggle glowing text with ink sacs, and wax with
 * honeycomb.
 */
class SignInteractionRule : BlockInteractionRule(
    BlockSet.fromTag("minecraft:standing_signs") +
        BlockSet.fromTag("minecraft:wall_signs") +
        BlockSet.fromTag("minecraft:all_hanging_signs"),
) {
    init {
        onInteract {
            if (isSecondaryUseActive()) return@onInteract

            val isFrontText = Signs.isFacingFrontText(block, player, blockPosition, blockFace)
            val nbt = block.nbtOrEmpty()
            val waxed = nbt.getBoolean("is_waxed")

            if (!waxed && !heldItem.isAir && applyItem(this, isFrontText, nbt)) {
                isBlockingItemUse = true
                return@onInteract
            }

            if (waxed) {
                isBlockingItemUse = true
                playBlockSound(
                    instance,
                    if (Signs.isHangingSign(block)) {
                        SoundEvent.BLOCK_HANGING_SIGN_WAXED_INTERACT_FAIL
                    } else {
                        SoundEvent.BLOCK_SIGN_WAXED_INTERACT_FAIL
                    },
                    blockCenter(blockPosition),
                )
                return@onInteract
            }

            // Anything else (including another sign) opens the editor; only sneaking places a block.
            if (!mayBuild(player)) return@onInteract
            isBlockingItemUse = true
            player.sendPacket(
                OpenSignEditorPacket(blockPosition, if (isFrontText) SignTextSlot.FRONT else SignTextSlot.BACK),
            )
        }

        interactionNode.addListener(PlayerEditSignEvent::class.java) { event ->
            val key = if (event.textSlot == SignTextSlot.FRONT) "front_text" else "back_text"
            val nbt = event.block.nbtOrEmpty()
            val text = nbt.getCompound(key)
            val messages = ListBinaryTag.from(event.lines.map(StringBinaryTag::stringBinaryTag))
            event.instance.setBlock(
                event.blockPosition,
                event.block.withNbt(nbt.put(key, text.put("messages", messages))),
            )
        }
    }

    /** Applies the held item to the sign text, returning whether the interaction was handled. */
    private fun applyItem(
        event: PlayerBlockInteractEvent,
        isFrontText: Boolean,
        nbt: CompoundBinaryTag,
    ): Boolean {
        val material = event.heldItem.material()
        val color = dyeColor(material)
        val applicator = material == Material.HONEYCOMB || material == Material.GLOW_INK_SAC ||
            material == Material.INK_SAC || color != null
        if (!applicator || !mayBuild(event.player)) return false

        val textKey = if (isFrontText) "front_text" else "back_text"
        val text = nbt.getCompound(textKey)
        val updatedNbt: CompoundBinaryTag
        val sound: SoundEvent?

        when (material) {
            Material.HONEYCOMB -> {
                updatedNbt = nbt.putBoolean("is_waxed", true)
                sound = null
            }
            Material.GLOW_INK_SAC -> {
                if (text.getBoolean("has_glowing_text")) return false
                updatedNbt = nbt.put(textKey, text.putBoolean("has_glowing_text", true))
                sound = SoundEvent.ITEM_GLOW_INK_SAC_USE
            }
            Material.INK_SAC -> {
                if (!text.getBoolean("has_glowing_text")) return false
                updatedNbt = nbt.put(textKey, text.putBoolean("has_glowing_text", false))
                sound = SoundEvent.ITEM_INK_SAC_USE
            }
            else -> {
                if (color == text.getString("color", "black")) return false
                updatedNbt = nbt.put(textKey, text.putString("color", color!!))
                sound = SoundEvent.ITEM_DYE_USE
            }
        }

        event.instance.setBlock(event.blockPosition, event.block.withNbt(updatedNbt))

        if (sound == null) {
            event.instance.sendGroupedPacket(
                WorldEventPacket(WorldEvent.PARTICLES_WAX_ON.id(), event.blockPosition, 0, false),
            )
        } else {
            playBlockSound(event.instance, sound, blockCenter(event.blockPosition))
        }

        event.consumeHeldItem(1)
        return true
    }

    private fun dyeColor(material: Material): String? {
        val path = material.key().value()
        return if (path.endsWith("_dye")) path.removeSuffix("_dye") else null
    }
}
