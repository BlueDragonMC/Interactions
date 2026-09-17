package com.bluedragonmc.interactions.rules.interaction

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.consumeHeldItem
import com.bluedragonmc.interactions.util.heldItem
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.ListBinaryTag
import net.minestom.server.component.DataComponents
import net.minestom.server.instance.block.Block

/**
 * Using a spawn egg on a monster spawner or trial spawner changes the entity it spawns.
 */
class SpawnerRule : PlacementRule(BlockSet.from(Block.SPAWNER, Block.TRIAL_SPAWNER)) {
    init {
        onInteract {
            if (!heldItem.material().key().value().endsWith("_spawn_egg")) return@onInteract
            val entityId = heldItem.get(DataComponents.ENTITY_DATA)?.type()?.key()?.asString() ?: return@onInteract

            val nbt = block.nbtOrEmpty()
            val state = when (block.key()) {
                Block.SPAWNER.key() -> block.withNbt(nbt.withMobSpawnData(entityId))
                Block.TRIAL_SPAWNER.key() -> block
                    .withProperty(States.TRIAL_SPAWNER_STATE, "inactive")
                    .withNbt(nbt.withTrialSpawnData(entityId))
                else -> return@onInteract
            }
            instance.setBlock(blockPosition, state)
            consumeHeldItem(1)
            isBlockingItemUse = true
        }
    }

    private fun CompoundBinaryTag.withMobSpawnData(entityId: String): CompoundBinaryTag {
        val entity = CompoundBinaryTag.builder().putString("id", entityId).build()
        val spawnData = CompoundBinaryTag.builder().put("entity", entity).build()
        return CompoundBinaryTag.builder().put(this).put("SpawnData", spawnData).build()
    }

    private fun CompoundBinaryTag.withTrialSpawnData(entityId: String): CompoundBinaryTag {
        val entity = CompoundBinaryTag.builder().putString("id", entityId).build()
        val spawnData = CompoundBinaryTag.builder().put("entity", entity).build()
        val weighted = CompoundBinaryTag.builder().put("data", spawnData).putInt("weight", 1).build()
        val potentials = ListBinaryTag.from(listOf(weighted))
        val normal = CompoundBinaryTag.builder()
            .put(getCompound("normal_config"))
            .put("spawn_potentials", potentials)
            .build()
        val ominous = CompoundBinaryTag.builder()
            .put(getCompound("ominous_config"))
            .put("spawn_potentials", potentials)
            .build()
        return CompoundBinaryTag.builder()
            .put(this)
            .put("normal_config", normal)
            .put("ominous_config", ominous)
            .build()
    }
}
