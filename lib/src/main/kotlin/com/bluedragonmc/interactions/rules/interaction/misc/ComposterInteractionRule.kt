package com.bluedragonmc.interactions.rules.interaction.misc

import com.bluedragonmc.interactions.BlockInteractionRule
import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.blockCenter
import com.bluedragonmc.interactions.util.consumeHeldItem
import com.bluedragonmc.interactions.util.dropItemStack
import com.bluedragonmc.interactions.util.getBlockOrAir
import com.bluedragonmc.interactions.util.heldMaterial
import com.bluedragonmc.interactions.util.isSecondaryUseActive
import com.bluedragonmc.interactions.util.playBlockSound
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.sound.SoundEvent
import net.minestom.server.timer.TaskSchedule
import java.util.concurrent.ThreadLocalRandom

/**
 * Composters accept compostable items with per-item chances, becoming ready to yield bone meal.
 */
class ComposterInteractionRule : BlockInteractionRule(BlockSet.from(Block.COMPOSTER)) {
    init {
        onInteract {
            if (isSecondaryUseActive()) return@onInteract
            val level = block.getProperty(States.LEVEL)?.toIntOrNull() ?: 0

            if (level == 8) {
                isBlockingItemUse = true
                extractBoneMeal(this)
                return@onInteract
            }

            val chance = chanceFor(heldMaterial)
            if (chance < 0f || level >= 7) {
                if (chance >= 0f) isBlockingItemUse = true
                return@onInteract
            }

            isBlockingItemUse = true
            val success = level == 0 || ThreadLocalRandom.current().nextFloat() < chance
            if (success) {
                val newLevel = level + 1
                instance.setBlock(blockPosition, block.withProperty(States.LEVEL, newLevel.toString()))
                if (newLevel == 7) scheduleReady(this)
            }

            instance.playBlockSound(
                if (success) SoundEvent.BLOCK_COMPOSTER_FILL_SUCCESS else SoundEvent.BLOCK_COMPOSTER_FILL,
                blockPosition.blockCenter(),
            )
            consumeHeldItem(1)
        }
    }

    private fun extractBoneMeal(event: PlayerBlockInteractEvent) {
        event.instance.dropItemStack(event.blockPosition, ItemStack.of(Material.BONE_MEAL), yOffset = 1.01)
        event.instance.setBlock(event.blockPosition, event.block.withProperty(States.LEVEL, "0"))
        event.instance.playBlockSound(SoundEvent.BLOCK_COMPOSTER_EMPTY, event.blockPosition.blockCenter())
    }

    private fun scheduleReady(event: PlayerBlockInteractEvent) {
        event.instance.scheduler().buildTask {
            val current = event.instance.getBlockOrAir(event.blockPosition)
            if (current.key() != Block.COMPOSTER.key() || current.getProperty(States.LEVEL) != "7") return@buildTask
            event.instance.setBlock(event.blockPosition, current.withProperty(States.LEVEL, "8"))
            event.instance.playBlockSound(SoundEvent.BLOCK_COMPOSTER_READY, event.blockPosition.blockCenter())
        }.delay(TaskSchedule.tick(20)).schedule()
    }

    private fun chanceFor(material: Material): Float {
        val path = material.key().value()
        return when (path) {
            in LOW -> 0.3f
            in LOW_MID -> 0.5f
            in MID -> 0.65f
            in MID_HIGH -> 0.85f
            in HIGH -> 1.0f
            else -> -1.0f
        }
    }

    private companion object {
        val LOW = setOf(
            "jungle_leaves", "oak_leaves", "spruce_leaves", "dark_oak_leaves", "pale_oak_leaves",
            "acacia_leaves", "cherry_leaves", "birch_leaves", "azalea_leaves", "mangrove_leaves",
            "oak_sapling", "spruce_sapling", "birch_sapling", "jungle_sapling", "acacia_sapling",
            "cherry_sapling", "dark_oak_sapling", "pale_oak_sapling", "mangrove_propagule",
            "beetroot_seeds", "dried_kelp", "short_grass", "kelp", "melon_seeds", "pumpkin_seeds",
            "seagrass", "sweet_berries", "glow_berries", "wheat_seeds", "moss_carpet", "pale_moss_carpet",
            "pale_hanging_moss", "pink_petals", "wildflowers", "leaf_litter", "small_dripleaf",
            "hanging_roots", "mangrove_roots", "torchflower_seeds", "pitcher_pod", "firefly_bush",
            "bush", "cactus_flower", "dry_short_grass", "dry_tall_grass",
        )
        val LOW_MID = setOf(
            "dried_kelp_block", "tall_grass", "flowering_azalea_leaves", "cactus", "sugar_cane", "vine",
            "nether_sprouts", "weeping_vines", "twisting_vines", "melon_slice", "glow_lichen",
        )
        val MID = setOf(
            "sea_pickle", "lily_pad", "pumpkin", "carved_pumpkin", "melon", "apple", "beetroot",
            "carrot", "cocoa_beans", "potato", "wheat", "brown_mushroom", "red_mushroom", "mushroom_stem",
            "crimson_fungus", "warped_fungus", "nether_wart", "crimson_roots", "warped_roots",
            "shroomlight", "dandelion", "poppy", "blue_orchid", "allium", "azure_bluet", "red_tulip",
            "orange_tulip", "white_tulip", "pink_tulip", "oxeye_daisy", "cornflower", "lily_of_the_valley",
            "wither_rose", "open_eyeblossom", "closed_eyeblossom", "fern", "sunflower", "lilac",
            "rose_bush", "peony", "large_fern", "spore_blossom", "azalea", "moss_block", "pale_moss_block",
            "big_dripleaf",
        )
        val MID_HIGH = setOf(
            "hay_block", "brown_mushroom_block", "red_mushroom_block", "nether_wart_block",
            "warped_wart_block", "flowering_azalea", "bread", "baked_potato", "cookie", "torchflower",
            "pitcher_plant",
        )
        val HIGH = setOf("cake", "pumpkin_pie")
    }
}
