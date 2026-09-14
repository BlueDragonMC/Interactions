package com.bluedragonmc.interactions.util

import net.kyori.adventure.key.Key
import net.minestom.server.instance.block.Block

object Tags {

    /** Resolves [key] to the set of block registry keys it contains, or an empty set if unknown. */
    fun blockTag(key: String): Set<Key> =
        Block.staticRegistry().getTag(Key.key(key))
            ?.mapTo(mutableSetOf()) { it.key() }
            ?: emptySet()

    /** The `minecraft:supports_vegetation` tag (flowers, saplings and similar ground plants). */
    val SUPPORTS_VEGETATION: Set<Key> by lazy { blockTag("minecraft:supports_vegetation") }

    /** The `minecraft:supports_crops` tag (farmland). */
    val SUPPORTS_CROPS: Set<Key> by lazy { blockTag("minecraft:supports_crops") }

    /** The `minecraft:supports_stem_crops` tag (farmland). */
    val SUPPORTS_STEM_CROPS: Set<Key> by lazy { blockTag("minecraft:supports_stem_crops") }

    /** The `minecraft:supports_sugar_cane` tag. */
    val SUPPORTS_SUGAR_CANE: Set<Key> by lazy { blockTag("minecraft:supports_sugar_cane") }

    /** The `minecraft:supports_sugar_cane_adjacently` tag. */
    val SUPPORTS_SUGAR_CANE_ADJACENTLY: Set<Key> by lazy { blockTag("minecraft:supports_sugar_cane_adjacently") }

    /** The `minecraft:supports_bamboo` tag. */
    val SUPPORTS_BAMBOO: Set<Key> by lazy { blockTag("minecraft:supports_bamboo") }

    /** The `minecraft:supports_cactus` tag. */
    val SUPPORTS_CACTUS: Set<Key> by lazy { blockTag("minecraft:supports_cactus") }

    /** The `minecraft:walls` tag. */
    val WALLS: Set<Key> by lazy { blockTag("minecraft:walls") }

    /** The `minecraft:unstable_bottom_center` tag. */
    val UNSTABLE_BOTTOM_CENTER: Set<Key> by lazy { blockTag("minecraft:unstable_bottom_center") }

    /** The `minecraft:shulker_boxes` tag. */
    val SHULKER_BOXES: Set<Key> by lazy { blockTag("minecraft:shulker_boxes") }

    /** The `minecraft:leaves` tag. */
    val LEAVES: Set<Key> by lazy { blockTag("minecraft:leaves") }
}
