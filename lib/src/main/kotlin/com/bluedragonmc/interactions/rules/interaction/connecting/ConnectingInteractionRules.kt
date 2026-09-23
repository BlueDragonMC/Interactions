package com.bluedragonmc.interactions.rules.interaction.connecting

import com.bluedragonmc.interactions.BlockInteractionRule
import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.blockCenter
import com.bluedragonmc.interactions.util.horizontalDirection
import com.bluedragonmc.interactions.util.isSecondaryUseActive
import com.bluedragonmc.interactions.util.playBlockSoundExcept
import net.minestom.server.instance.block.Block
import net.minestom.server.sound.SoundEvent
import java.util.concurrent.ThreadLocalRandom

/** Fence gates open by hand, re-facing toward the player when opened from the opposite side. */
class FenceGateInteractionRule : BlockInteractionRule(BlockSet.fromTag("minecraft:fence_gates")) {
    init {
        onInteract {
            if (isSecondaryUseActive()) return@onInteract
            isBlockingItemUse = true

            val open = block.getProperty(States.OPEN) == "true"
            val updated = if (open) {
                block.withProperty(States.OPEN, "false")
            } else {
                val facing = player.position.horizontalDirection()
                var state = block
                if (state.getProperty(States.FACING) == facing.oppositeFace.name.lowercase()) {
                    state = state.withProperty(States.FACING, facing.name.lowercase())
                }
                state.withProperty(States.OPEN, "true")
            }
            instance.setBlock(blockPosition, updated)

            instance.playBlockSoundExcept(
                player,
                gateSound(block, !open),
                blockPosition.blockCenter(),
                pitch = ThreadLocalRandom.current().nextFloat() * 0.1f + 0.9f,
            )
        }
    }

    private fun gateSound(block: Block, opening: Boolean): SoundEvent = when (block.key().value()) {
        "bamboo_fence_gate" ->
            if (opening) SoundEvent.BLOCK_BAMBOO_WOOD_FENCE_GATE_OPEN else SoundEvent.BLOCK_BAMBOO_WOOD_FENCE_GATE_CLOSE
        "cherry_fence_gate" ->
            if (opening) SoundEvent.BLOCK_CHERRY_WOOD_FENCE_GATE_OPEN else SoundEvent.BLOCK_CHERRY_WOOD_FENCE_GATE_CLOSE
        "crimson_fence_gate", "warped_fence_gate" ->
            if (opening) SoundEvent.BLOCK_NETHER_WOOD_FENCE_GATE_OPEN else SoundEvent.BLOCK_NETHER_WOOD_FENCE_GATE_CLOSE
        else -> if (opening) SoundEvent.BLOCK_FENCE_GATE_OPEN else SoundEvent.BLOCK_FENCE_GATE_CLOSE
    }
}
