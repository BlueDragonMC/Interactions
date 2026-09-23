package com.bluedragonmc.interactions.util

import net.kyori.adventure.key.Key
import net.minestom.server.instance.block.Block
import net.minestom.server.item.Material

object Tags {

    /** Resolves [key] to the set of block registry keys it contains, or an empty set if unknown. */
    fun blockTag(key: String): Set<Key> =
        Block.staticRegistry().getTag(Key.key(key))
            ?.mapTo(mutableSetOf()) { it.key() }
            ?: emptySet()

    /** Resolves [key] to the set of item/material registry keys it contains, or an empty set if unknown. */
    fun materialTag(key: String): Set<Key> =
        Material.staticRegistry().getTag(Key.key(key))
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

    /** The `minecraft:speleothems` tag (pointed dripstone, sulfur spikes). */
    val SPELEOTHEMS: Set<Key> by lazy { blockTag("minecraft:speleothems") }

    /** The `minecraft:wall_post_override` tag (torches, signs, banners, pressure plates, ...). */
    val WALL_POST_OVERRIDE: Set<Key> by lazy { blockTag("minecraft:wall_post_override") }

    /** The `minecraft:soul_fire_base_blocks` tag (soul sand, soul soil). */
    val SOUL_FIRE_BASE_BLOCKS: Set<Key> by lazy { blockTag("minecraft:soul_fire_base_blocks") }

    /** The `minecraft:wooden_buttons` tag. */
    val WOODEN_BUTTONS: Set<Key> by lazy { blockTag("minecraft:wooden_buttons") }

    /** The `minecraft:copper_doors` tag. */
    val COPPER_DOORS: Set<Key> by lazy { blockTag("minecraft:copper_doors") }

    /** The `minecraft:copper_trapdoors` tag. */
    val COPPER_TRAPDOORS: Set<Key> by lazy { blockTag("minecraft:copper_trapdoors") }

    /** The `minecraft:note_block_top_instruments` material tag (blocks that can be placed on top). */
    val NOTE_BLOCK_TOP_INSTRUMENTS: Set<Key> by lazy { materialTag("minecraft:note_block_top_instruments") }

    /** The `minecraft:snow` tag (snow, snow blocks). */
    val SNOW: Set<Key> by lazy { blockTag("minecraft:snow") }

    /** The `minecraft:maintains_farmland` tag (crops that keep farmland hydrated regardless). */
    val MAINTAINS_FARMLAND: Set<Key> by lazy { blockTag("minecraft:maintains_farmland") }

    /** The `minecraft:supports_chorus_plant` tag. */
    val SUPPORTS_CHORUS_PLANT: Set<Key> by lazy { blockTag("minecraft:supports_chorus_plant") }

    /** The `minecraft:pale_oak_logs` tag. */
    val PALE_OAK_LOGS: Set<Key> by lazy { blockTag("minecraft:pale_oak_logs") }

    /** The `minecraft:causes_continuous_geyser_eruptions` tag (magma-like blocks). */
    val CONTINUOUS_GEYSER: Set<Key> by lazy { blockTag("minecraft:causes_continuous_geyser_eruptions") }

    /** The `minecraft:causes_periodic_geyser_eruptions` tag (soul sand). */
    val PERIODIC_GEYSER: Set<Key> by lazy { blockTag("minecraft:causes_periodic_geyser_eruptions") }
}
