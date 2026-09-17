package com.bluedragonmc.interactions.util

import net.minestom.server.entity.GameMode
import net.minestom.server.entity.PlayerHand
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

/** The item stack held in the hand used for this interaction. */
val PlayerBlockInteractEvent.heldItem: ItemStack
    get() = player.getItemInHand(hand)

/** The material held in the hand used for this interaction. */
val PlayerBlockInteractEvent.heldMaterial: Material
    get() = heldItem.material()

/**
 * Whether the targeted block's interaction should be blocked because the player was crouching.
 */
fun PlayerBlockInteractEvent.isSecondaryUseActive(): Boolean =
    player.isSneaking && (!heldItem.isAir || !player.getItemInHand(PlayerHand.OFF).isAir)

/** Replaces the item held in the hand used for this interaction. */
fun PlayerBlockInteractEvent.setHeldItem(item: ItemStack) {
    player.setItemInHand(hand, item)
}

/**
 * Consumes [amount] from the held item, leaving the slot empty when the stack runs out.
 */
fun PlayerBlockInteractEvent.consumeHeldItem(amount: Int) {
    if (player.gameMode == GameMode.CREATIVE) return
    setHeldItem(heldItem.consume(amount))
}
