package com.bluedragonmc.interactions

import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockHandler
import net.minestom.server.tag.Tag

/**
 * Provides [BlockHandler]s that allow clients to see information from block entities that are part of an existing world.
 */
object BlockHandlers {
    // https://minecraft.wiki/w/Block_entity#List_of_block_entities
    private val blockHandlers = mapOf(
        BlockSet.from(Block.BEEHIVE, Block.BEE_NEST) to listOf(
            Tag.NBT("bees").list(),
            Tag.Integer("flower_pos").list()
        ),
        BlockSet.fromTag("minecraft:signs") + BlockSet.fromTag("minecraft:all_hanging_signs") to listOf(
            Tag.Boolean("is_waxed"),
            Tag.NBT("front_text"),
            Tag.NBT("back_text"),
        ),
        BlockSet.from(Block.SCULK_SENSOR, Block.CALIBRATED_SCULK_SENSOR) to listOf(
            Tag.Integer("last_vibration_frequency"),
            Tag.NBT("listener")
        ),
        BlockSet.from(Block.SCULK_CATALYST) to listOf(
            Tag.NBT("cursors").list()
        ),
        BlockSet.from(Block.SCULK_SHRIEKER) to listOf(
            Tag.NBT("VibrationListener"),
        ),
        BlockSet.fromTag("minecraft:banners") to listOf(
            Tag.Component("CustomName"),
            Tag.NBT("patterns").list()
        ),
        BlockSet.from(
            Block.CHEST, Block.TRAPPED_CHEST, Block.BARREL, Block.DROPPER, Block.DISPENSER
        ) + BlockSet.fromTag("minecraft:shulker_boxes") to listOf(
            Tag.Component("CustomName"),
            Tag.NBT("lock"),
            Tag.String("LootTable"),
            Tag.Long("LootTableSeed"),
            Tag.Component("Items").list(),
        ),
        BlockSet.from(Block.CHISELED_BOOKSHELF) to listOf(
            Tag.NBT("Items").list(),
            Tag.Integer("last_interacted_slot")
        ),
        BlockSet.from(Block.DECORATED_POT) to listOf(
            Tag.String("LootTable"),
            Tag.Long("LootTableSeed"),
            Tag.NBT("item"),
            Tag.String("sherds").list()
        ),
        BlockSet.from(Block.FURNACE, Block.SMOKER, Block.BLAST_FURNACE) to listOf(
            Tag.NBT("Items").list(),
            Tag.Short("lit_time_remaining"),
            Tag.Short("cooking_time_spent"),
            Tag.Short("cooking_total_time"),
            Tag.Short("lit_total_time"),
            Tag.NBT("RecipesUsed")
        ),
        BlockSet.from(Block.CAMPFIRE, Block.SOUL_CAMPFIRE) to listOf(
            Tag.Component("Items").list(),
            Tag.Integer("CookingTimes").list(),
            Tag.Integer("CookingTotalTimes").list()
        ),
        BlockSet.from(Block.BREWING_STAND) to listOf(
            Tag.Component("Items").list(),
            Tag.Short("BrewTime"),
            Tag.Byte("Fuel")
        ),
        BlockSet.from(Block.HOPPER) to listOf(
            Tag.Component("Items").list(),
            Tag.Integer("TransferCooldown")
        ),
        BlockSet.from(Block.LECTERN) to listOf(
            Tag.Component("Book"),
            Tag.Integer("Page")
        ),
        BlockSet.from(Block.CRAFTER) to listOf(
            Tag.Component("Items").list(),
            Tag.Integer("disabled_slots").list(),
            Tag.Boolean("triggered"),
            Tag.Integer("crafting_ticks_remaining")
        ),
        BlockSet.from(Block.BEACON) to listOf(
            Tag.Component("CustomName"),
            Tag.NBT("lock"),
            Tag.String("primary_effect"),
            Tag.String("secondary_effect"),
        ),
        BlockSet.from(Block.CREAKING_HEART) to listOf(
            Tag.Integer("creaking").list()
        ),
        BlockSet.from(Block.SPAWNER) to listOf(
            Tag.Short("Delay"),
            Tag.Short("MaxNearbyEntities"),
            Tag.Short("MaxSpawnDelay"),
            Tag.Short("MinSpawnDelay"),
            Tag.Short("RequiredPlayerRange"),
            Tag.Short("SpawnCount"),
            Tag.NBT("SpawnData"),
            Tag.NBT("SpawnPotentials").list(),
            Tag.Short("SpawnRange")
        ),
        BlockSet.from(Block.TRIAL_SPAWNER) to listOf(
            Tag.Integer("required_player_range"),
            Tag.Integer("target_cooldown_length"),
            Tag.NBT("normal_config"),
            Tag.NBT("ominous_config"),
            Tag.Integer("registered_players").list().list(),
            Tag.Integer("current_mobs").list().list(),
            Tag.Long("cooldown_ends_at"),
            Tag.Long("next_mob_spawns_at"),
            Tag.Integer("total_mobs_spawned"),
            Tag.NBT("spawn_data"),
            Tag.String("ejecting_loot_table")
        ),
        BlockSet.from(Block.VAULT) to listOf(
            Tag.NBT("config"),
            Tag.NBT("server_data"),
            Tag.NBT("shared_data")
        ),
        BlockSet.from(Block.MOVING_PISTON) to listOf(
            Tag.NBT("blockState"),
            Tag.Boolean("extending"),
            Tag.Integer("facing"),
            Tag.Float("progress"),
            Tag.Boolean("source")
        ),
        BlockSet.from(Block.JUKEBOX) to listOf(
            Tag.NBT("RecordItem"),
            Tag.Long("ticks_since_song_started")
        ),
        BlockSet.from(Block.ENCHANTING_TABLE) to listOf(
            Tag.Component("CustomName")
        ),
        BlockSet.from(Block.END_GATEWAY) to listOf(
            Tag.Long("Age"),
            Tag.Boolean("ExactTeleport"),
            Tag.Integer("exit_portal").list()
        ),
        BlockSet.from(Block.PLAYER_HEAD) to listOf(
            Tag.String("custom_name"),
            Tag.String("note_block_sound"),
            Tag.NBT("profile")
        ),
        BlockSet.from(
            Block.ZOMBIE_HEAD,
            Block.SKELETON_SKULL,
            Block.WITHER_SKELETON_SKULL,
            Block.CREEPER_HEAD,
            Block.DRAGON_HEAD,
            Block.PIGLIN_HEAD
        ) to listOf(
            Tag.String("custom_name"),
        ),
        BlockSet.from(Block.COMMAND_BLOCK, Block.CHAIN_COMMAND_BLOCK, Block.REPEATING_COMMAND_BLOCK) to listOf(
            Tag.Boolean("auto"),
            Tag.String("Command"),
            Tag.Boolean("conditionMet"),
            Tag.Long("LastExecution"),
            Tag.String("LastOutput"),
            Tag.Boolean("powered"),
            Tag.Integer("SuccessCount"),
            Tag.Boolean("TrackOutput"),
            Tag.Boolean("UpdateLastExecution"),
            Tag.Component("CustomName")
        ),
        BlockSet.from(Block.STRUCTURE_BLOCK) to listOf(
            Tag.String("author"),
            Tag.Boolean("ignoreEntities"),
            Tag.Float("integrity"),
            Tag.String("metadata"),
            Tag.String("mirror"),
            Tag.String("mode"),
            Tag.String("name"),
            Tag.Integer("posX"),
            Tag.Integer("posY"),
            Tag.Integer("posZ"),
            Tag.Boolean("powered"),
            Tag.String("rotation"),
            Tag.Long("seed"),
            Tag.Boolean("showboundingbox"),
            Tag.Integer("sizeX"),
            Tag.Integer("sizeY"),
            Tag.Integer("sizeZ"),
        ),
        BlockSet.from(Block.JIGSAW) to listOf(
            Tag.String("final_state"),
            Tag.String("joint"),
            Tag.String("name"),
            Tag.String("pool"),
            Tag.String("target"),
            Tag.String("selection_priority"),
            Tag.String("placement_priority"),
        ),
        BlockSet.from(Block.COMPARATOR) to listOf(
            Tag.Integer("OutputSignal")
        ),
        BlockSet.from(Block.CONDUIT) to listOf(
            Tag.Integer("Target").list()
        ),
        BlockSet.from(Block.SUSPICIOUS_SAND, Block.SUSPICIOUS_GRAVEL) to listOf(
            Tag.String("LootTable"),
            Tag.Long("LootTableSeed"),
            Tag.NBT("item"),
        ),
        BlockSet.fromTag("minecraft:wooden_shelves") to listOf(
            Tag.NBT("Items").list(),
            Tag.Boolean("align_items_to_bottom")
        )
    )

    private val minestomHandlers = blockHandlers.flatMap { (blockSet, tags) ->
        blockSet.getBlocks().map { block ->
            object : BlockHandler {
                override fun getKey() = block.key()
                override fun getBlockEntityTags() = tags
            }
        }
    }

    internal fun getAll() = minestomHandlers
}