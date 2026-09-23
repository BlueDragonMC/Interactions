package com.bluedragonmc.interactions.rules.interaction.wall

import com.bluedragonmc.interactions.BlockInteractionRule
import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.axisName
import com.bluedragonmc.interactions.util.blockCenter
import com.bluedragonmc.interactions.util.isSecondaryUseActive
import com.bluedragonmc.interactions.util.playBlockSound
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.network.packet.server.play.BlockActionPacket
import net.minestom.server.sound.SoundEvent

/** Bells ring when hit from a proper side face. */
class BellInteractionRule : BlockInteractionRule(BlockSet.from(Block.BELL)) {
    init {
        onInteract {
            if (isSecondaryUseActive()) return@onInteract
            val attachment = block.getProperty(States.ATTACHMENT) ?: return@onInteract
            val facing = block.getProperty(States.FACING)?.let(::faceOf) ?: return@onInteract
            if (!isProperHit(attachment, facing, blockFace, cursorPosition.y())) return@onInteract

            isBlockingItemUse = true
            instance.playBlockSound(SoundEvent.BLOCK_BELL_USE, blockPosition.blockCenter(), 2.0f, 1.0f)
            instance.sendGroupedPacket(
                BlockActionPacket(
                    blockPosition,
                    BELL_RING_EVENT.toByte(),
                    blockFace.toDirection().ordinal.toByte(),
                    block,
                ),
            )
        }
    }

    private fun isProperHit(attachment: String, facing: BlockFace, clickedFace: BlockFace, clickY: Double): Boolean {
        if (clickedFace.toDirection().vertical() || clickY > BELL_BODY_TOP) return false
        return when (attachment) {
            "floor" -> facing.axisName == clickedFace.axisName
            "ceiling" -> true
            else -> facing.axisName != clickedFace.axisName
        }
    }

    private companion object {
        /** Trigger the bell swinging animation on the client. */
        const val BELL_RING_EVENT = 1

        /**
         * The top of the bell body. The bell shape ends at `13/16 = 0.8125`.
         * `0.8124` is that edge nudged down, so that clicking the attachment
         * post above the bell does not ring it.
         */
        const val BELL_BODY_TOP = 0.8124
    }
}

private fun faceOf(name: String): BlockFace? = when (name) {
    "north" -> BlockFace.NORTH
    "east" -> BlockFace.EAST
    "south" -> BlockFace.SOUTH
    "west" -> BlockFace.WEST
    else -> null
}
