package com.bluedragonmc.interactions.rules.interaction.misc

import com.bluedragonmc.interactions.BlockInteractionRule
import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.Tags
import com.bluedragonmc.interactions.util.blockCenter
import com.bluedragonmc.interactions.util.getBlockOrAir
import com.bluedragonmc.interactions.util.heldMaterial
import com.bluedragonmc.interactions.util.isSecondaryUseActive
import net.kyori.adventure.sound.Sound
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.sound.SoundEvent
import kotlin.math.pow

/**
 * Right-clicking a note block increments its `note` and plays the instrument sound for the block below.
 */
class NoteBlockInteractionRule : BlockInteractionRule(BlockSet.from(Block.NOTE_BLOCK)) {
    init {
        onInteract {
            // "Top instruments" (e.g. mob heads) may be placed on top of the note block instead.
            if (blockFace == BlockFace.TOP && heldMaterial.key() in Tags.NOTE_BLOCK_TOP_INSTRUMENTS) return@onInteract
            if (isSecondaryUseActive()) return@onInteract
            isBlockingItemUse = true

            val note = block.getProperty(States.NOTE)?.toIntOrNull() ?: 0
            val nextNote = (note + 1) % 25
            instance.setBlock(blockPosition, block.withProperty(States.NOTE, nextNote.toString()))

            val instrument = block.getProperty(States.INSTRUMENT)
            if (!isHeadInstrument(instrument) && !instance.getBlockOrAir(blockPosition.relative(BlockFace.TOP)).air()) {
                return@onInteract
            }

            val soundEvent = instrumentSound(instrument) ?: return@onInteract
            val pitch = if (isHeadInstrument(instrument)) 1.0f else 2.0.pow((nextNote - 12) / 12.0).toFloat()
            val sound = Sound.sound(soundEvent, Sound.Source.RECORD, 3.0f, pitch)
            val center = blockPosition.blockCenter()
            instance.playSound(sound, center.x(), center.y(), center.z())
        }
    }

    private fun isHeadInstrument(instrument: String?): Boolean = when (instrument) {
        "skeleton", "wither_skeleton", "zombie", "custom_head", "creeper", "dragon", "piglin" -> true
        else -> false
    }

    private fun instrumentSound(instrument: String?): SoundEvent? = when (instrument) {
        "bass" -> SoundEvent.BLOCK_NOTE_BLOCK_BASS
        "snare" -> SoundEvent.BLOCK_NOTE_BLOCK_SNARE
        "hat" -> SoundEvent.BLOCK_NOTE_BLOCK_HAT
        "basedrum" -> SoundEvent.BLOCK_NOTE_BLOCK_BASEDRUM
        "bell" -> SoundEvent.BLOCK_NOTE_BLOCK_BELL
        "flute" -> SoundEvent.BLOCK_NOTE_BLOCK_FLUTE
        "chime" -> SoundEvent.BLOCK_NOTE_BLOCK_CHIME
        "guitar" -> SoundEvent.BLOCK_NOTE_BLOCK_GUITAR
        "xylophone" -> SoundEvent.BLOCK_NOTE_BLOCK_XYLOPHONE
        "iron_xylophone" -> SoundEvent.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE
        "cow_bell" -> SoundEvent.BLOCK_NOTE_BLOCK_COW_BELL
        "didgeridoo" -> SoundEvent.BLOCK_NOTE_BLOCK_DIDGERIDOO
        "bit" -> SoundEvent.BLOCK_NOTE_BLOCK_BIT
        "banjo" -> SoundEvent.BLOCK_NOTE_BLOCK_BANJO
        "pling" -> SoundEvent.BLOCK_NOTE_BLOCK_PLING
        "trumpet" -> SoundEvent.BLOCK_NOTE_BLOCK_TRUMPET
        "trumpet_exposed" -> SoundEvent.BLOCK_NOTE_BLOCK_TRUMPET_EXPOSED
        "trumpet_weathered" -> SoundEvent.BLOCK_NOTE_BLOCK_TRUMPET_WEATHERED
        "trumpet_oxidized" -> SoundEvent.BLOCK_NOTE_BLOCK_TRUMPET_OXIDIZED
        "zombie" -> SoundEvent.BLOCK_NOTE_BLOCK_IMITATE_ZOMBIE
        "skeleton" -> SoundEvent.BLOCK_NOTE_BLOCK_IMITATE_SKELETON
        "creeper" -> SoundEvent.BLOCK_NOTE_BLOCK_IMITATE_CREEPER
        "dragon" -> SoundEvent.BLOCK_NOTE_BLOCK_IMITATE_ENDER_DRAGON
        "wither_skeleton" -> SoundEvent.BLOCK_NOTE_BLOCK_IMITATE_WITHER_SKELETON
        "piglin" -> SoundEvent.BLOCK_NOTE_BLOCK_IMITATE_PIGLIN
        "custom_head" -> null
        null, "harp" -> SoundEvent.BLOCK_NOTE_BLOCK_HARP
        else -> SoundEvent.BLOCK_NOTE_BLOCK_HARP
    }
}
