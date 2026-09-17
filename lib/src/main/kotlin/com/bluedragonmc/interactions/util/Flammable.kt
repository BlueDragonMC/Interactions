package com.bluedragonmc.interactions.util

import net.kyori.adventure.key.Key
import net.minestom.server.instance.block.Block

/**
 * Blocks that are considered flammable, used to decide whether a fire block can survive against them.
 */
object Flammable {

    private val TAGS = listOf(
        "minecraft:planks",
        "minecraft:wooden_slabs",
        "minecraft:wooden_stairs",
        "minecraft:wooden_fences",
        "minecraft:fence_gates",
        "minecraft:wooden_doors",
        "minecraft:wooden_trapdoors",
        "minecraft:logs_that_burn",
        "minecraft:leaves",
        "minecraft:wool",
        "minecraft:wool_carpets",
        "minecraft:saplings",
        "minecraft:flowers",
        "minecraft:crops",
        "minecraft:wooden_shelves",
    )

    private val EXPLICIT = setOf(
        "minecraft:bamboo_mosaic",
        "minecraft:bamboo_mosaic_slab",
        "minecraft:bamboo_mosaic_stairs",
        "minecraft:bamboo_block",
        "minecraft:stripped_bamboo_block",
        "minecraft:mangrove_roots",
        "minecraft:bookshelf",
        "minecraft:tnt",
        "minecraft:short_grass",
        "minecraft:fern",
        "minecraft:dead_bush",
        "minecraft:short_dry_grass",
        "minecraft:tall_dry_grass",
        "minecraft:tall_grass",
        "minecraft:large_fern",
        "minecraft:vine",
        "minecraft:coal_block",
        "minecraft:hay_block",
        "minecraft:target",
        "minecraft:pale_moss_block",
        "minecraft:pale_moss_carpet",
        "minecraft:pale_hanging_moss",
        "minecraft:dried_kelp_block",
        "minecraft:bamboo",
        "minecraft:scaffolding",
        "minecraft:lectern",
        "minecraft:composter",
        "minecraft:sweet_berry_bush",
        "minecraft:beehive",
        "minecraft:bee_nest",
        "minecraft:cave_vines",
        "minecraft:cave_vines_plant",
        "minecraft:spore_blossom",
        "minecraft:big_dripleaf",
        "minecraft:big_dripleaf_stem",
        "minecraft:small_dripleaf",
        "minecraft:hanging_roots",
        "minecraft:glow_lichen",
        "minecraft:firefly_bush",
        "minecraft:bush",
        "minecraft:leaf_litter",
        "minecraft:cactus_flower",
    )

    private val NON_FLAMMABLE_WOOD = setOf(
        "crimson_planks", "warped_planks",
        "crimson_slab", "warped_slab",
        "crimson_stairs", "warped_stairs",
        "crimson_fence", "warped_fence",
        "crimson_fence_gate", "warped_fence_gate",
        "crimson_door", "warped_door",
        "crimson_trapdoor", "warped_trapdoor",
    )

    /** Registry keys of blocks that can ignite fire placed next to them. */
    val BLOCKS: Set<Key> by lazy {
        buildSet {
            TAGS.forEach { addAll(Tags.blockTag(it)) }
            EXPLICIT.forEach { key -> Block.fromKey(key)?.let { add(it.key()) } }
            NON_FLAMMABLE_WOOD.forEach { key -> Block.fromKey("minecraft:$key")?.let { remove(it.key()) } }
        }
    }
}
