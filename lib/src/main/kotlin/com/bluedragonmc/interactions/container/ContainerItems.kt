package com.bluedragonmc.interactions.container

import net.kyori.adventure.nbt.BinaryTagTypes
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.ListBinaryTag
import net.kyori.adventure.nbt.NumberBinaryTag
import net.minestom.server.MinecraftServer
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.registry.Registries

/** The block-entity tag vanilla uses to store a container's contents. */
const val ITEMS_TAG = "Items"

/** The per-item tag holding the inventory slot, as written by vanilla. */
private const val SLOT_TAG = "Slot"

/**
 * Reads the container contents stored on [block] into a list indexed by inventory slot.
 *
 * The list always has [size] entries; empty (or missing) slots are [ItemStack.AIR].
 */
fun readContainerItems(block: Block, size: Int): List<ItemStack> {
    val items = MutableList(size) { ItemStack.AIR }
    val list = block.nbtOrEmpty().getList(ITEMS_TAG, BinaryTagTypes.COMPOUND)
    for (tag in list) {
        if (tag !is CompoundBinaryTag) continue
        val slot = (tag.get(SLOT_TAG) as? NumberBinaryTag)?.numberValue()?.toInt() ?: continue
        if (slot !in 0 until size) continue
        items[slot] = decodeItem(tag)
    }
    return items
}

/** Reads the item in [slot] from the container contents stored on [block]. */
fun readContainerItem(block: Block, slot: Int): ItemStack {
    val list = block.nbtOrEmpty().getList(ITEMS_TAG, BinaryTagTypes.COMPOUND)
    for (tag in list) {
        if (tag !is CompoundBinaryTag) continue
        if ((tag.get(SLOT_TAG) as? NumberBinaryTag)?.numberValue()?.toInt() != slot) continue
        return decodeItem(tag)
    }
    return ItemStack.AIR
}

/**
 * Returns a copy of this block with its container contents replaced by the non-empty stacks in
 * [items], keeping every other block-entity tag intact.
 */
fun Block.withContainerItems(
    items: List<ItemStack>,
    registries: Registries = MinecraftServer.getRegistries(),
): Block {
    val entries = items.mapIndexedNotNull { slot, stack ->
        if (stack.isAir) null
        else CompoundBinaryTag.builder()
            .put(stack.toItemNBT(registries))
            .putByte(SLOT_TAG, slot.toByte())
            .build()
    }
    val nbt = CompoundBinaryTag.builder().put(nbtOrEmpty())
    if (entries.isEmpty()) {
        nbt.remove(ITEMS_TAG)
    } else {
        nbt.put(ITEMS_TAG, ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, entries))
    }
    return withNbt(nbt.build())
}

private fun decodeItem(tag: CompoundBinaryTag): ItemStack =
    runCatching { ItemStack.fromItemNBT(tag.remove(SLOT_TAG), MinecraftServer.getRegistries()) }
        .getOrDefault(ItemStack.AIR)
