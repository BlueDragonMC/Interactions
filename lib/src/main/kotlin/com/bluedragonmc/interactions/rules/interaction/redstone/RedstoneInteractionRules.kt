package com.bluedragonmc.interactions.rules.interaction.redstone

import com.bluedragonmc.interactions.BlockInteractionRule
import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.Tags
import com.bluedragonmc.interactions.util.blockCenter
import com.bluedragonmc.interactions.util.getBlockOrAir
import com.bluedragonmc.interactions.util.isSecondaryUseActive
import com.bluedragonmc.interactions.util.playBlockSound
import com.bluedragonmc.interactions.util.playBlockSoundExcept
import net.minestom.server.instance.block.Block
import net.minestom.server.sound.SoundEvent
import net.minestom.server.timer.TaskSchedule

/** Wooden buttons stay pressed for 30 ticks, stone ones for 20. */
class ButtonInteractionRule : BlockInteractionRule(BlockSet.fromTag("minecraft:buttons")) {
    init {
        onInteract {
            if (isSecondaryUseActive()) return@onInteract
            isBlockingItemUse = true
            if (block.getProperty(States.POWERED) == "true") return@onInteract

            val wooden = block.key() in Tags.WOODEN_BUTTONS
            instance.setBlock(blockPosition, block.withProperty(States.POWERED, "true"))
            instance.playBlockSoundExcept(
                player,
                if (wooden) SoundEvent.BLOCK_WOODEN_BUTTON_CLICK_ON else SoundEvent.BLOCK_STONE_BUTTON_CLICK_ON,
                blockPosition.blockCenter(),
                0.3f,
                0.6f,
            )

            instance.scheduler().buildTask {
                val current = instance.getBlockOrAir(blockPosition)
                if (current.key() !in keys) return@buildTask
                if (current.getProperty(States.POWERED) != "true") return@buildTask
                instance.setBlock(blockPosition, current.withProperty(States.POWERED, "false"))
                instance.playBlockSound(
                    if (wooden) SoundEvent.BLOCK_WOODEN_BUTTON_CLICK_OFF else SoundEvent.BLOCK_STONE_BUTTON_CLICK_OFF,
                    blockPosition.blockCenter(),
                    0.3f,
                    0.5f,
                )
            }.delay(TaskSchedule.tick(if (wooden) WOODEN_DELAY else STONE_DELAY)).schedule()
        }
    }

    private companion object {
        const val WOODEN_DELAY = 30
        const val STONE_DELAY = 20
    }
}
