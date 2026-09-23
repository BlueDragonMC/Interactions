package com.bluedragonmc.interactions.rules.interaction.plants

import com.bluedragonmc.interactions.BlockInteractionRule
import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.consumeHeldItem
import com.bluedragonmc.interactions.util.heldMaterial
import com.bluedragonmc.interactions.util.isSecondaryUseActive
import net.minestom.server.instance.block.Block
import net.minestom.server.item.Material
import net.minestom.server.sound.SoundEvent

/**
 * Cave vines (both the head and body): right-click harvests glow berries when present,
 * and bone meal grows them.
 */
class CaveVinesInteractionRule : BlockInteractionRule(
    BlockSet.from(Block.CAVE_VINES, Block.CAVE_VINES_PLANT),
) {
    init {
        onInteract {
            val hasBerries = block.getProperty(States.BERRIES) == "true"

            if (!hasBerries && heldMaterial == Material.BONE_MEAL) {
                isBlockingItemUse = true
                instance.setBlock(blockPosition, block.withProperty(States.BERRIES, "true"))
                sendBoneMealParticles(this)
                consumeHeldItem(1)
                return@onInteract
            }

            if (!hasBerries || isSecondaryUseActive()) return@onInteract
            isBlockingItemUse = true
            harvest(this, Material.GLOW_BERRIES, 1, SoundEvent.BLOCK_CAVE_VINES_PICK_BERRIES)
            instance.setBlock(blockPosition, block.withProperty(States.BERRIES, "false"))
        }
    }
}
