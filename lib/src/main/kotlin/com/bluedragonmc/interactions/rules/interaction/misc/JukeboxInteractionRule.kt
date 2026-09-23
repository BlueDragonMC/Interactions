package com.bluedragonmc.interactions.rules.interaction.misc

import com.bluedragonmc.interactions.BlockInteractionRule
import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.consumeHeldItem
import com.bluedragonmc.interactions.util.dropItemStack
import com.bluedragonmc.interactions.util.heldItem
import com.bluedragonmc.interactions.util.isSecondaryUseActive
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.minestom.server.MinecraftServer
import net.minestom.server.component.DataComponents
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.network.packet.server.play.WorldEventPacket
import net.minestom.server.worldevent.WorldEvent

/**
 * Jukeboxes accept a music disc (playing the song) and eject it when used again.
 */
class JukeboxInteractionRule : BlockInteractionRule(BlockSet.from(Block.JUKEBOX)) {
    init {
        onInteract {
            if (isSecondaryUseActive()) return@onInteract
            isBlockingItemUse = true

            if (block.getProperty(States.HAS_RECORD) == "true") {
                ejectRecord(this)
                return@onInteract
            }

            val song = heldItem.get(DataComponents.JUKEBOX_PLAYABLE) ?: return@onInteract
            val record = heldItem.withAmount(1)
            val nbt = CompoundBinaryTag.builder()
                .put(block.nbtOrEmpty())
                .put(RECORD_ITEM, record.toItemNBT(MinecraftServer.getRegistries()))
                .putLong("ticks_since_song_started", 0L)
                .build()
            instance.setBlock(blockPosition, block.withProperty(States.HAS_RECORD, "true").withNbt(nbt))

            val songId = MinecraftServer.getJukeboxSongRegistry().getId(song)
            instance.sendGroupedPacket(WorldEventPacket(WorldEvent.SOUND_PLAY_JUKEBOX_SONG.id(), blockPosition, songId, false))
            consumeHeldItem(1)
        }
    }

    private fun ejectRecord(event: PlayerBlockInteractEvent) {
        val recordTag = event.block.nbtOrEmpty().getCompound(RECORD_ITEM)
        val cleared = event.block.nbtOrEmpty().remove(RECORD_ITEM).remove("ticks_since_song_started")
        event.instance.setBlock(event.blockPosition, event.block.withProperty(States.HAS_RECORD, "false").withNbt(cleared))

        if (!recordTag.isEmpty) {
            val record = ItemStack.fromItemNBT(recordTag, MinecraftServer.getRegistries())
            event.instance.dropItemStack(event.blockPosition, record, yOffset = 1.0)
        }

        event.instance.sendGroupedPacket(
            WorldEventPacket(WorldEvent.SOUND_STOP_JUKEBOX_SONG.id(), event.blockPosition, 0, false),
        )
    }

    private companion object {
        const val RECORD_ITEM = "RecordItem"
    }
}
