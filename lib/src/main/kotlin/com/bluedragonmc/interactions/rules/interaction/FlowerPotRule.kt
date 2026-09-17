package com.bluedragonmc.interactions.rules.interaction

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.plus
import com.bluedragonmc.interactions.util.consumeHeldItem
import com.bluedragonmc.interactions.util.heldItem
import com.bluedragonmc.interactions.util.isSecondaryUseActive
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack

class FlowerPotRule : PlacementRule(
    BlockSet.from(Block.FLOWER_POT) + BlockSet.matching { it.startsWith("minecraft:potted_") },
) {
    init {
        onInteract {
            if (isSecondaryUseActive()) return@onInteract

            if (block.key() == Block.FLOWER_POT.key()) plant() else removePlant()
            isBlockingItemUse = true
        }
    }
}

private fun PlayerBlockInteractEvent.plant() {
    val potted = heldItem.material().block()?.let(::pottedBlock) ?: return
    instance.setBlock(blockPosition, potted)
    consumeHeldItem(1)
}

private fun PlayerBlockInteractEvent.removePlant() {
    // A compatible flower just consumes the interaction; the pot keeps its current content.
    if (heldItem.material().block()?.let(::pottedBlock) != null) return

    val content = contentBlock(block) ?: return
    val item = content.material()?.let(ItemStack::of) ?: return
    instance.setBlock(blockPosition, Block.FLOWER_POT)
    if (!player.inventory.addItemStack(item)) player.dropItem(item)
}

private fun pottedBlock(content: Block): Block? = when (content) {
    Block.AZALEA -> Block.POTTED_AZALEA_BUSH
    Block.FLOWERING_AZALEA -> Block.POTTED_FLOWERING_AZALEA_BUSH
    else -> Block.fromKey("minecraft:potted_${content.key().value()}")
}

private fun contentBlock(potted: Block): Block? = when (potted) {
    Block.POTTED_AZALEA_BUSH -> Block.AZALEA
    Block.POTTED_FLOWERING_AZALEA_BUSH -> Block.FLOWERING_AZALEA
    else -> Block.fromKey("minecraft:${potted.key().value().removePrefix("potted_")}")
}
