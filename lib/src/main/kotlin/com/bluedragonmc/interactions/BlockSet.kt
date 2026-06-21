package com.bluedragonmc.interactions

import net.kyori.adventure.key.Key
import net.kyori.adventure.key.KeyPattern
import net.minestom.server.instance.block.Block

interface BlockSet {
    fun getBlocks(): Collection<Block>

    companion object {
        fun fromTag(@KeyPattern tag: String) = TagBlockSet(tag)
        fun from(vararg blocks: Block) = EnumeratedBlockSet(blocks.asList())
    }
}

class EnumeratedBlockSet(private val blocks: List<Block>) : BlockSet {
    override fun getBlocks(): Collection<Block> = blocks
}

// Inspired by https://github.com/everbuild-org/blocks-and-stuff/blob/38e3791bf49edbf86acc743482f62c2b2afdddb8/blocksandstuff-common/src/main/kotlin/org/everbuild/blocksandstuff/common/tag/MinestomBlockTagProvider.kt#L12
class TagBlockSet(private val tag: String): BlockSet {
    override fun getBlocks(): Collection<Block> = Block.staticRegistry().getTag(Key.key(tag))?.mapNotNull {
        Block.fromKey(it.key())
    }?.toSet() ?: error("No blocks found in tag $tag")
}

class CombinedBlockSet(private val left: BlockSet, private val right: BlockSet): BlockSet {
    override fun getBlocks(): Collection<Block> = (left.getBlocks() + right.getBlocks()).toSet()
}

operator fun BlockSet.plus(other: BlockSet) = CombinedBlockSet(this, other)
