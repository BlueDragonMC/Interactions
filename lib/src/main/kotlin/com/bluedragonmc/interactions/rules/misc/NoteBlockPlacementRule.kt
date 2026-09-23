package com.bluedragonmc.interactions.rules.misc

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.getBlockOrAir
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace

/**
 * Note blocks take their `instrument` from the block above when it is a mob head, otherwise from the
 * block below.
 */
class NoteBlockPlacementRule : PlacementRule(BlockSet.from(Block.NOTE_BLOCK)) {
    init {
        onPlace { block = setInstrument(instance, blockPosition, block) }

        onUpdate {
            if (fromFace != BlockFace.TOP && fromFace != BlockFace.BOTTOM) return@onUpdate
            resultBlock = setInstrument(instance, blockPosition, block)
        }
    }

    private fun setInstrument(instance: Instance, position: BlockVec, noteBlock: Block): Block {
        val above = instrumentFor(instance.getBlockOrAir(position.relative(BlockFace.TOP)))
        val instrument = if (isHeadInstrument(above)) {
            above
        } else {
            instrumentFor(instance.getBlockOrAir(position.relative(BlockFace.BOTTOM)))
        }
        val resolved = if (isHeadInstrument(instrument) && !isHeadInstrument(above)) "harp" else instrument
        return noteBlock.withProperty(States.INSTRUMENT, resolved)
    }
}

private fun instrumentFor(block: Block): String {
    val path = block.key().value()

    if (path in COPPER_INSTRUMENT_BLOCKS) {
        return when {
            path.contains("oxidized") -> "trumpet_oxidized"
            path.contains("weathered") -> "trumpet_weathered"
            path.contains("exposed") -> "trumpet_exposed"
            else -> "trumpet"
        }
    }

    if (isColorVariant(path, "wool") || isColorVariant(path, "wool_stairs") || isColorVariant(path, "wool_slab")) {
        return "guitar"
    }
    if (isColorVariant(path, "stained_glass") || isColorVariant(path, "stained_glass_pane") || path in HAT_BLOCKS) {
        return "hat"
    }
    if (isColorVariant(path, "concrete_powder") || path in SNARE_BLOCKS) return "snare"
    if (isColorVariant(path, "terracotta") || isColorVariant(path, "glazed_terracotta") ||
        isColorVariant(path, "concrete") || path in BASE_DRUM_BLOCKS
    ) {
        return "basedrum"
    }
    if (isColorVariant(path, "banner") || isColorVariant(path, "wall_banner") || path in BASS_BLOCKS) return "bass"

    return when (path) {
        "creeper_head" -> "creeper"
        "dragon_head" -> "dragon"
        "piglin_head" -> "piglin"
        "player_head" -> "custom_head"
        "skeleton_skull" -> "skeleton"
        "wither_skeleton_skull" -> "wither_skeleton"
        "zombie_head" -> "zombie"
        "bone_block" -> "xylophone"
        "clay" -> "flute"
        "emerald_block" -> "bit"
        "glowstone" -> "pling"
        "gold_block" -> "bell"
        "hay_block" -> "banjo"
        "iron_block" -> "iron_xylophone"
        "packed_ice" -> "chime"
        "pumpkin" -> "didgeridoo"
        "soul_sand" -> "cow_bell"
        else -> "harp"
    }
}

private fun isHeadInstrument(instrument: String): Boolean = instrument in HEAD_INSTRUMENTS

private fun isColorVariant(path: String, suffix: String): Boolean =
    COLORS.any { path == "${it}_$suffix" }

private val HEAD_INSTRUMENTS = setOf(
    "skeleton", "wither_skeleton", "zombie", "custom_head", "creeper", "dragon", "piglin",
)

private val BASE_DRUM_BLOCKS = setOf(
    "andesite", "andesite_slab", "andesite_stairs", "andesite_wall", "basalt", "bedrock", "blackstone",
    "blackstone_slab", "blackstone_stairs", "blackstone_wall", "blast_furnace", "brain_coral_block", "brick_slab",
    "brick_stairs", "brick_wall", "bricks", "bubble_coral_block", "calcite", "chiseled_cinnabar",
    "chiseled_deepslate", "chiseled_nether_bricks", "chiseled_polished_blackstone", "chiseled_quartz_block",
    "chiseled_red_sandstone", "chiseled_resin_bricks", "chiseled_sandstone", "chiseled_stone_bricks",
    "chiseled_sulfur", "chiseled_tuff", "chiseled_tuff_bricks", "cinnabar", "cinnabar_brick_slab",
    "cinnabar_brick_stairs", "cinnabar_brick_wall", "cinnabar_bricks", "cinnabar_slab", "cinnabar_stairs",
    "cinnabar_wall", "coal_block", "coal_ore", "cobbled_deepslate", "cobbled_deepslate_slab",
    "cobbled_deepslate_stairs", "cobbled_deepslate_wall", "cobblestone", "cobblestone_slab", "cobblestone_stairs",
    "cobblestone_wall", "copper_ore", "cracked_deepslate_bricks", "cracked_deepslate_tiles", "cracked_nether_bricks",
    "cracked_polished_blackstone_bricks", "cracked_stone_bricks", "creaking_heart", "crimson_nylium",
    "crying_obsidian", "cut_red_sandstone", "cut_red_sandstone_slab", "cut_sandstone", "cut_sandstone_slab",
    "dark_prismarine", "dark_prismarine_slab", "dark_prismarine_stairs", "dead_brain_coral", "dead_brain_coral_block",
    "dead_brain_coral_fan", "dead_brain_coral_wall_fan", "dead_bubble_coral", "dead_bubble_coral_block",
    "dead_bubble_coral_fan", "dead_bubble_coral_wall_fan", "dead_fire_coral", "dead_fire_coral_block",
    "dead_fire_coral_fan", "dead_fire_coral_wall_fan", "dead_horn_coral", "dead_horn_coral_block",
    "dead_horn_coral_fan", "dead_horn_coral_wall_fan", "dead_tube_coral", "dead_tube_coral_block",
    "dead_tube_coral_fan", "dead_tube_coral_wall_fan", "deepslate", "deepslate_brick_slab", "deepslate_brick_stairs",
    "deepslate_brick_wall", "deepslate_bricks", "deepslate_coal_ore", "deepslate_copper_ore", "deepslate_diamond_ore",
    "deepslate_emerald_ore", "deepslate_gold_ore", "deepslate_iron_ore", "deepslate_lapis_ore",
    "deepslate_redstone_ore", "deepslate_tile_slab", "deepslate_tile_stairs", "deepslate_tile_wall",
    "deepslate_tiles", "diamond_ore", "diorite", "diorite_slab", "diorite_stairs", "diorite_wall", "dispenser",
    "dripstone_block", "dropper", "emerald_ore", "enchanting_table", "end_portal_frame", "end_stone",
    "end_stone_brick_slab", "end_stone_brick_stairs", "end_stone_brick_wall", "end_stone_bricks", "ender_chest",
    "fire_coral_block", "furnace", "gilded_blackstone", "gold_ore", "granite", "granite_slab", "granite_stairs",
    "granite_wall", "horn_coral_block", "iron_ore", "lapis_ore", "magma_block", "mossy_cobblestone",
    "mossy_cobblestone_slab", "mossy_cobblestone_stairs", "mossy_cobblestone_wall", "mossy_stone_brick_slab",
    "mossy_stone_brick_stairs", "mossy_stone_brick_wall", "mossy_stone_bricks", "mud_brick_slab", "mud_brick_stairs",
    "mud_brick_wall", "mud_bricks", "nether_brick_fence", "nether_brick_slab", "nether_brick_stairs",
    "nether_brick_wall", "nether_bricks", "nether_gold_ore", "nether_quartz_ore", "netherrack", "observer",
    "obsidian", "petrified_oak_slab", "pointed_dripstone", "polished_andesite", "polished_andesite_slab",
    "polished_andesite_stairs", "polished_basalt", "polished_blackstone", "polished_blackstone_brick_slab",
    "polished_blackstone_brick_stairs", "polished_blackstone_brick_wall", "polished_blackstone_bricks",
    "polished_blackstone_pressure_plate", "polished_blackstone_slab", "polished_blackstone_stairs",
    "polished_blackstone_wall", "polished_cinnabar", "polished_cinnabar_slab", "polished_cinnabar_stairs",
    "polished_cinnabar_wall", "polished_deepslate", "polished_deepslate_slab", "polished_deepslate_stairs",
    "polished_deepslate_wall", "polished_diorite", "polished_diorite_slab", "polished_diorite_stairs",
    "polished_granite", "polished_granite_slab", "polished_granite_stairs", "polished_sulfur", "polished_sulfur_slab",
    "polished_sulfur_stairs", "polished_sulfur_wall", "polished_tuff", "polished_tuff_slab", "polished_tuff_stairs",
    "polished_tuff_wall", "potent_sulfur", "prismarine", "prismarine_brick_slab", "prismarine_brick_stairs",
    "prismarine_bricks", "prismarine_slab", "prismarine_stairs", "prismarine_wall", "purpur_block", "purpur_pillar",
    "purpur_slab", "purpur_stairs", "quartz_block", "quartz_bricks", "quartz_pillar", "quartz_slab", "quartz_stairs",
    "raw_copper_block", "raw_gold_block", "raw_iron_block", "red_nether_brick_slab", "red_nether_brick_stairs",
    "red_nether_brick_wall", "red_nether_bricks", "red_sandstone", "red_sandstone_slab", "red_sandstone_stairs",
    "red_sandstone_wall", "redstone_ore", "reinforced_deepslate", "resin_block", "resin_brick_slab",
    "resin_brick_stairs", "resin_brick_wall", "resin_bricks", "respawn_anchor", "sandstone", "sandstone_slab",
    "sandstone_stairs", "sandstone_wall", "smoker", "smooth_basalt", "smooth_quartz", "smooth_quartz_slab",
    "smooth_quartz_stairs", "smooth_red_sandstone", "smooth_red_sandstone_slab", "smooth_red_sandstone_stairs",
    "smooth_sandstone", "smooth_sandstone_slab", "smooth_sandstone_stairs", "smooth_stone", "smooth_stone_slab",
    "spawner", "stone", "stone_brick_slab", "stone_brick_stairs", "stone_brick_wall", "stone_bricks",
    "stone_pressure_plate", "stone_slab", "stone_stairs", "stonecutter", "sulfur", "sulfur_brick_slab",
    "sulfur_brick_stairs", "sulfur_brick_wall", "sulfur_bricks", "sulfur_slab", "sulfur_spike", "sulfur_stairs",
    "sulfur_wall", "terracotta", "trial_spawner", "tube_coral_block", "tuff", "tuff_brick_slab", "tuff_brick_stairs",
    "tuff_brick_wall", "tuff_bricks", "tuff_slab", "tuff_stairs", "tuff_wall", "vault", "warped_nylium",
)

private val BASS_BLOCKS = setOf(
    "acacia_door", "acacia_fence", "acacia_fence_gate", "acacia_hanging_sign", "acacia_log", "acacia_planks",
    "acacia_pressure_plate", "acacia_shelf", "acacia_sign", "acacia_slab", "acacia_stairs", "acacia_trapdoor",
    "acacia_wall_hanging_sign", "acacia_wall_sign", "acacia_wood", "bamboo_block", "bamboo_door", "bamboo_fence",
    "bamboo_fence_gate", "bamboo_hanging_sign", "bamboo_mosaic", "bamboo_mosaic_slab", "bamboo_mosaic_stairs",
    "bamboo_planks", "bamboo_pressure_plate", "bamboo_shelf", "bamboo_sign", "bamboo_slab", "bamboo_stairs",
    "bamboo_trapdoor", "bamboo_wall_hanging_sign", "bamboo_wall_sign", "barrel", "bee_nest", "beehive", "birch_door",
    "birch_fence", "birch_fence_gate", "birch_hanging_sign", "birch_log", "birch_planks", "birch_pressure_plate",
    "birch_shelf", "birch_sign", "birch_slab", "birch_stairs", "birch_trapdoor", "birch_wall_hanging_sign",
    "birch_wall_sign", "birch_wood", "bookshelf", "brown_mushroom_block", "campfire", "cartography_table",
    "cherry_door", "cherry_fence", "cherry_fence_gate", "cherry_hanging_sign", "cherry_log", "cherry_planks",
    "cherry_pressure_plate", "cherry_shelf", "cherry_sign", "cherry_slab", "cherry_stairs", "cherry_trapdoor",
    "cherry_wall_hanging_sign", "cherry_wall_sign", "cherry_wood", "chest", "chiseled_bookshelf", "composter",
    "crafting_table", "crimson_door", "crimson_fence", "crimson_fence_gate", "crimson_hanging_sign", "crimson_hyphae",
    "crimson_planks", "crimson_pressure_plate", "crimson_shelf", "crimson_sign", "crimson_slab", "crimson_stairs",
    "crimson_stem", "crimson_trapdoor", "crimson_wall_hanging_sign", "crimson_wall_sign", "dark_oak_door",
    "dark_oak_fence", "dark_oak_fence_gate", "dark_oak_hanging_sign", "dark_oak_log", "dark_oak_planks",
    "dark_oak_pressure_plate", "dark_oak_shelf", "dark_oak_sign", "dark_oak_slab", "dark_oak_stairs",
    "dark_oak_trapdoor", "dark_oak_wall_hanging_sign", "dark_oak_wall_sign", "dark_oak_wood", "daylight_detector",
    "fletching_table", "jukebox", "jungle_door", "jungle_fence", "jungle_fence_gate", "jungle_hanging_sign",
    "jungle_log", "jungle_planks", "jungle_pressure_plate", "jungle_shelf", "jungle_sign", "jungle_slab",
    "jungle_stairs", "jungle_trapdoor", "jungle_wall_hanging_sign", "jungle_wall_sign", "jungle_wood", "lectern",
    "loom", "mangrove_door", "mangrove_fence", "mangrove_fence_gate", "mangrove_hanging_sign", "mangrove_log",
    "mangrove_planks", "mangrove_pressure_plate", "mangrove_roots", "mangrove_shelf", "mangrove_sign",
    "mangrove_slab", "mangrove_stairs", "mangrove_trapdoor", "mangrove_wall_hanging_sign", "mangrove_wall_sign",
    "mangrove_wood", "mushroom_stem", "note_block", "oak_door", "oak_fence", "oak_fence_gate", "oak_hanging_sign",
    "oak_log", "oak_planks", "oak_pressure_plate", "oak_shelf", "oak_sign", "oak_slab", "oak_stairs", "oak_trapdoor",
    "oak_wall_hanging_sign", "oak_wall_sign", "oak_wood", "pale_oak_door", "pale_oak_fence", "pale_oak_fence_gate",
    "pale_oak_hanging_sign", "pale_oak_log", "pale_oak_planks", "pale_oak_pressure_plate", "pale_oak_shelf",
    "pale_oak_sign", "pale_oak_slab", "pale_oak_stairs", "pale_oak_trapdoor", "pale_oak_wall_hanging_sign",
    "pale_oak_wall_sign", "pale_oak_wood", "poplar_door", "poplar_fence", "poplar_fence_gate", "poplar_hanging_sign",
    "poplar_log", "poplar_planks", "poplar_pressure_plate", "poplar_shelf", "poplar_sign", "poplar_slab",
    "poplar_stairs", "poplar_trapdoor", "poplar_wall_hanging_sign", "poplar_wall_sign", "poplar_wood",
    "red_mushroom_block", "smithing_table", "soul_campfire", "spruce_door", "spruce_fence", "spruce_fence_gate",
    "spruce_hanging_sign", "spruce_log", "spruce_planks", "spruce_pressure_plate", "spruce_shelf", "spruce_sign",
    "spruce_slab", "spruce_stairs", "spruce_trapdoor", "spruce_wall_hanging_sign", "spruce_wall_sign", "spruce_wood",
    "stripped_acacia_log", "stripped_acacia_wood", "stripped_bamboo_block", "stripped_birch_log",
    "stripped_birch_wood", "stripped_cherry_log", "stripped_cherry_wood", "stripped_crimson_hyphae",
    "stripped_crimson_stem", "stripped_dark_oak_log", "stripped_dark_oak_wood", "stripped_jungle_log",
    "stripped_jungle_wood", "stripped_mangrove_log", "stripped_mangrove_wood", "stripped_oak_log",
    "stripped_oak_wood", "stripped_pale_oak_log", "stripped_pale_oak_wood", "stripped_poplar_log",
    "stripped_poplar_wood", "stripped_spruce_log", "stripped_spruce_wood", "stripped_warped_hyphae",
    "stripped_warped_stem", "trapped_chest", "warped_door", "warped_fence", "warped_fence_gate",
    "warped_hanging_sign", "warped_hyphae", "warped_planks", "warped_pressure_plate", "warped_shelf", "warped_sign",
    "warped_slab", "warped_stairs", "warped_stem", "warped_trapdoor", "warped_wall_hanging_sign", "warped_wall_sign",
)

private val HAT_BLOCKS = setOf(
    "beacon", "conduit", "glass", "glass_pane", "sea_lantern", "tinted_glass",
)

private val SNARE_BLOCKS = setOf(
    "gravel", "heavy_core", "red_sand", "sand", "suspicious_gravel", "suspicious_sand",
)

private val COPPER_INSTRUMENT_BLOCKS = setOf(
    "chiseled_copper", "copper_block", "cut_copper", "cut_copper_slab", "cut_copper_stairs",
    "exposed_chiseled_copper", "exposed_copper", "exposed_cut_copper", "exposed_cut_copper_slab",
    "exposed_cut_copper_stairs", "oxidized_chiseled_copper", "oxidized_copper", "oxidized_cut_copper",
    "oxidized_cut_copper_slab", "oxidized_cut_copper_stairs", "waxed_chiseled_copper", "waxed_copper_block",
    "waxed_cut_copper", "waxed_cut_copper_slab", "waxed_cut_copper_stairs", "waxed_exposed_chiseled_copper",
    "waxed_exposed_copper", "waxed_exposed_cut_copper", "waxed_exposed_cut_copper_slab",
    "waxed_exposed_cut_copper_stairs", "waxed_oxidized_chiseled_copper", "waxed_oxidized_copper",
    "waxed_oxidized_cut_copper", "waxed_oxidized_cut_copper_slab", "waxed_oxidized_cut_copper_stairs",
    "waxed_weathered_chiseled_copper", "waxed_weathered_copper", "waxed_weathered_cut_copper",
    "waxed_weathered_cut_copper_slab", "waxed_weathered_cut_copper_stairs", "weathered_chiseled_copper",
    "weathered_copper", "weathered_cut_copper", "weathered_cut_copper_slab", "weathered_cut_copper_stairs",
)

private val COLORS = setOf(
    "black", "blue", "brown", "cyan", "gray", "green", "light_blue", "light_gray", "lime", "magenta", "orange",
    "pink", "purple", "red", "white", "yellow",
)
