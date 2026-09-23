package com.bluedragonmc.interactions.util

import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.PlayerBlockInteractEvent

/** Damages the held item by [amount] durability, unless the player is in creative mode. */
fun PlayerBlockInteractEvent.damageHeldItem(amount: Int) {
    if (player.gameMode == GameMode.CREATIVE) return
    setHeldItem(heldItem.damage(amount))
}
