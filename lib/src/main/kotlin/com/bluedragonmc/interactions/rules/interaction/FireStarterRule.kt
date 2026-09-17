package com.bluedragonmc.interactions.rules.interaction

import com.bluedragonmc.interactions.InteractionRule
import com.bluedragonmc.interactions.util.Fire
import com.bluedragonmc.interactions.util.consumeHeldItem
import com.bluedragonmc.interactions.util.getBlockOrAir
import com.bluedragonmc.interactions.util.heldMaterial
import net.minestom.server.item.Material

/**
 * Flint and steel and fire charges create a fire block on the face of the clicked block.
 *
 * Fire charges are consumed; flint and steel is not. The resulting fire state is computed by [Fire].
 * Candles are lit by their own rule, which runs first and blocks item use.
 */
class FireStarterRule : InteractionRule() {
    init {
        onInteract {
            if (isBlockingItemUse) return@onInteract
            val consumesItem = when (heldMaterial) {
                Material.FLINT_AND_STEEL -> false
                Material.FIRE_CHARGE -> true
                else -> return@onInteract
            }

            val target = blockPosition.relative(blockFace)
            if (!instance.getBlockOrAir(target).air()) return@onInteract
            val fire = Fire.stateAt(instance, target) ?: return@onInteract

            instance.setBlock(target, fire)
            if (consumesItem) consumeHeldItem(1)
            isBlockingItemUse = true
        }
    }
}
