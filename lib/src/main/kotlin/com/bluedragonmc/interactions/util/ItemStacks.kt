package com.bluedragonmc.interactions.util

import net.minestom.server.coordinate.Point
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.ItemEntity
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.instance.Instance
import net.minestom.server.item.ItemStack

/** Damages the held item by [amount] durability, unless the player is in creative mode. */
fun PlayerBlockInteractEvent.damageHeldItem(amount: Int) {
    if (player.gameMode == GameMode.CREATIVE) return
    setHeldItem(heldItem.damage(amount))
}

/** Drops [stack] as an item entity at the block's center, offset vertically by [yOffset]. */
fun Instance.dropItemStack(position: Point, stack: ItemStack, yOffset: Double = 0.5) {
    if (stack.isAir) return
    val entity = ItemEntity(stack)
    entity.setInstance(this, position.add(0.5, yOffset, 0.5))
}

/** Adds [stack] to the player's inventory or drops it at [position] when the inventory is full. */
fun Player.giveOrDrop(instance: Instance, position: Point, stack: ItemStack) {
    if (stack.isAir) return
    if (!inventory.addItemStack(stack)) instance.dropItemStack(position, stack)
}
