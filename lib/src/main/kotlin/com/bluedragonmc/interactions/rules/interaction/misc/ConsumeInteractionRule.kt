package com.bluedragonmc.interactions.rules.interaction.misc

import com.bluedragonmc.interactions.BlockInteractionRule
import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.util.isSecondaryUseActive
import net.minestom.server.instance.block.Block

/**
 * Consumes right-clicks on blocks whose right-click action is supposed to open a menu, so
 * that the held item is not placed on them.
 */
class ConsumeInteractionRule : BlockInteractionRule(
    BlockSet.from(
        Block.ENDER_CHEST,
        Block.FURNACE,
        Block.BLAST_FURNACE,
        Block.SMOKER,
        Block.CRAFTER,
        Block.BREWING_STAND,
        Block.BEACON,
        Block.ENCHANTING_TABLE,
        Block.ANVIL,
        Block.CHIPPED_ANVIL,
        Block.DAMAGED_ANVIL,
        Block.CRAFTING_TABLE,
        Block.CARTOGRAPHY_TABLE,
        Block.SMITHING_TABLE,
        Block.GRINDSTONE,
        Block.LOOM,
        Block.STONECUTTER,
        Block.LECTERN,
    ),
) {
    init {
        onInteract {
            if (isSecondaryUseActive()) return@onInteract
            // TODO: replace with the real menu-opening interaction where the server provides one.
            isBlockingItemUse = true
        }
    }
}
