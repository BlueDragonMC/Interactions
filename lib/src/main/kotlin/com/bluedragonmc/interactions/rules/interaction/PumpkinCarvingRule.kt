package com.bluedragonmc.interactions.rules.interaction

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.heldMaterial
import com.bluedragonmc.interactions.util.horizontalDirection
import com.bluedragonmc.interactions.util.isSecondaryUseActive
import net.minestom.server.instance.block.Block
import net.minestom.server.item.Material

/**
 * Shearing an uncarved pumpkin turns it into a carved pumpkin.
 */
class PumpkinCarvingRule : PlacementRule(BlockSet.from(Block.PUMPKIN)) {
    init {
        onInteract {
            if (isSecondaryUseActive()) return@onInteract
            if (heldMaterial != Material.SHEARS) return@onInteract
            val facing = if (blockFace.toDirection().vertical()) {
                player.position.horizontalDirection().oppositeFace
            } else {
                blockFace
            }
            instance.setBlock(
                blockPosition,
                Block.CARVED_PUMPKIN.withProperty(States.FACING, facing.name.lowercase()),
            )
            isBlockingItemUse = true
        }
    }
}
