package com.bluedragonmc.interactions.rules.interaction.wall

import com.bluedragonmc.interactions.BlockInteractionRule
import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.blockCenter
import com.bluedragonmc.interactions.util.isSecondaryUseActive
import com.bluedragonmc.interactions.util.playBlockSoundExcept
import net.minestom.server.instance.block.Block
import net.minestom.server.sound.SoundEvent

/** Levers toggle their `powered` state when used, with a pitch that differs per state. */
class LeverInteractionRule : BlockInteractionRule(BlockSet.from(Block.LEVER)) {
    init {
        onInteract {
            if (isSecondaryUseActive()) return@onInteract
            isBlockingItemUse = true

            val powered = block.getProperty(States.POWERED) == "true"
            val newPowered = !powered
            instance.setBlock(blockPosition, block.withProperty(States.POWERED, newPowered.toString()))
            instance.playBlockSoundExcept(
                player,
                SoundEvent.BLOCK_LEVER_CLICK,
                blockPosition.blockCenter(),
                0.3f,
                if (newPowered) 0.6f else 0.5f,
            )
        }
    }
}
