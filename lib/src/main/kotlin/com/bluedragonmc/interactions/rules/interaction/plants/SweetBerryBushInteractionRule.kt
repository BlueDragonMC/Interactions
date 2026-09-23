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
import java.util.concurrent.ThreadLocalRandom

/**
 * Sweet berry bushes: bone meal advances the growth stage, and a grown bush is harvested on
 * right-click.
 */
class SweetBerryBushInteractionRule : BlockInteractionRule(BlockSet.from(Block.SWEET_BERRY_BUSH)) {
    init {
        onInteract {
            val age = block.getProperty(States.AGE)?.toIntOrNull() ?: 0

            if (age < 3 && heldMaterial == Material.BONE_MEAL) {
                isBlockingItemUse = true
                instance.setBlock(blockPosition, block.withProperty(States.AGE, (age + 1).toString()))
                sendBoneMealParticles(this)
                consumeHeldItem(1)
                return@onInteract
            }

            if (age <= 1 || isSecondaryUseActive()) return@onInteract
            isBlockingItemUse = true
            val amount = ThreadLocalRandom.current().nextInt(1, 3) + if (age == 3) 1 else 0
            harvest(this, Material.SWEET_BERRIES, amount, SoundEvent.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES)
            instance.setBlock(blockPosition, block.withProperty(States.AGE, "1"))
        }
    }
}
