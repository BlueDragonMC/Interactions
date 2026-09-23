package com.bluedragonmc.interactions.rules.misc

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.Tags
import com.bluedragonmc.interactions.util.getBlockOrAir
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace

/** Grass blocks, mycelium and podzol track whether a snow layer sits above them. */
class SnowyRule : PlacementRule(BlockSet.from(Block.GRASS_BLOCK, Block.MYCELIUM, Block.PODZOL)) {
    init {
        onPlace {
            val above = instance.getBlockOrAir(blockPosition.relative(BlockFace.TOP))
            block = block.withProperty(States.SNOWY, (above.key() in Tags.SNOW).toString())
        }

        onUpdate {
            if (fromFace != BlockFace.TOP) return@onUpdate
            val above = instance.getBlockOrAir(blockPosition.relative(BlockFace.TOP))
            resultBlock = block.withProperty(States.SNOWY, (above.key() in Tags.SNOW).toString())
        }
    }
}
