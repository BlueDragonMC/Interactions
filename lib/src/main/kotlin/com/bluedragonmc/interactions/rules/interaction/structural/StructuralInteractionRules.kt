package com.bluedragonmc.interactions.rules.interaction.structural

import com.bluedragonmc.interactions.BlockInteractionRule
import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.Tags
import com.bluedragonmc.interactions.util.blockCenter
import com.bluedragonmc.interactions.util.getBlockOrAir
import com.bluedragonmc.interactions.util.isSecondaryUseActive
import com.bluedragonmc.interactions.util.playBlockSoundExcept
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.sound.SoundEvent
import java.util.concurrent.ThreadLocalRandom

/** Wooden/copper/bamboo/cherry/nether doors open by hand; iron doors only respond to redstone. */
class DoorInteractionRule : BlockInteractionRule(BlockSet.fromTag("minecraft:doors")) {
    init {
        onInteract {
            if (isSecondaryUseActive()) return@onInteract
            if (block.key() == Block.IRON_DOOR.key()) return@onInteract
            isBlockingItemUse = true

            val open = block.getProperty(States.OPEN) != "true"
            instance.setBlock(blockPosition, block.withProperty(States.OPEN, open.toString()))

            val partnerPosition = if (block.getProperty(States.HALF) == "lower") {
                blockPosition.relative(BlockFace.TOP)
            } else {
                blockPosition.relative(BlockFace.BOTTOM)
            }
            val partner = instance.getBlockOrAir(partnerPosition)
            if (partner.key() == block.key()) {
                instance.setBlock(partnerPosition, partner.withProperty(States.OPEN, open.toString()))
            }

            instance.playBlockSoundExcept(
                player,
                doorSound(block, open),
                blockPosition.blockCenter(),
                pitch = ThreadLocalRandom.current().nextFloat() * 0.1f + 0.9f,
            )
        }
    }

    private fun doorSound(block: Block, opening: Boolean): SoundEvent {
        val key = block.key()
        return when {
            key in Tags.COPPER_DOORS -> if (opening) SoundEvent.BLOCK_COPPER_DOOR_OPEN else SoundEvent.BLOCK_COPPER_DOOR_CLOSE
            key.value() == "bamboo_door" -> if (opening) SoundEvent.BLOCK_BAMBOO_WOOD_DOOR_OPEN else SoundEvent.BLOCK_BAMBOO_WOOD_DOOR_CLOSE
            key.value() == "cherry_door" -> if (opening) SoundEvent.BLOCK_CHERRY_WOOD_DOOR_OPEN else SoundEvent.BLOCK_CHERRY_WOOD_DOOR_CLOSE
            key.value() == "crimson_door" || key.value() == "warped_door" ->
                if (opening) SoundEvent.BLOCK_NETHER_WOOD_DOOR_OPEN else SoundEvent.BLOCK_NETHER_WOOD_DOOR_CLOSE
            else -> if (opening) SoundEvent.BLOCK_WOODEN_DOOR_OPEN else SoundEvent.BLOCK_WOODEN_DOOR_CLOSE
        }
    }
}

/** Wooden/copper/bamboo/cherry/nether trapdoors open by hand; iron trapdoors only respond to redstone. */
class TrapdoorInteractionRule : BlockInteractionRule(BlockSet.fromTag("minecraft:trapdoors")) {
    init {
        onInteract {
            if (isSecondaryUseActive()) return@onInteract
            if (block.key() == Block.IRON_TRAPDOOR.key()) return@onInteract
            isBlockingItemUse = true

            val open = block.getProperty(States.OPEN) != "true"
            instance.setBlock(blockPosition, block.withProperty(States.OPEN, open.toString()))

            instance.playBlockSoundExcept(
                player,
                trapdoorSound(block, open),
                blockPosition.blockCenter(),
                pitch = ThreadLocalRandom.current().nextFloat() * 0.1f + 0.9f,
            )
        }
    }

    private fun trapdoorSound(block: Block, opening: Boolean): SoundEvent {
        val key = block.key()
        return when {
            key in Tags.COPPER_TRAPDOORS ->
                if (opening) SoundEvent.BLOCK_COPPER_TRAPDOOR_OPEN else SoundEvent.BLOCK_COPPER_TRAPDOOR_CLOSE
            key.value() == "bamboo_trapdoor" ->
                if (opening) SoundEvent.BLOCK_BAMBOO_WOOD_TRAPDOOR_OPEN else SoundEvent.BLOCK_BAMBOO_WOOD_TRAPDOOR_CLOSE
            key.value() == "cherry_trapdoor" ->
                if (opening) SoundEvent.BLOCK_CHERRY_WOOD_TRAPDOOR_OPEN else SoundEvent.BLOCK_CHERRY_WOOD_TRAPDOOR_CLOSE
            key.value() == "crimson_trapdoor" || key.value() == "warped_trapdoor" ->
                if (opening) SoundEvent.BLOCK_NETHER_WOOD_TRAPDOOR_OPEN else SoundEvent.BLOCK_NETHER_WOOD_TRAPDOOR_CLOSE
            else -> if (opening) SoundEvent.BLOCK_WOODEN_TRAPDOOR_OPEN else SoundEvent.BLOCK_WOODEN_TRAPDOOR_CLOSE
        }
    }
}
