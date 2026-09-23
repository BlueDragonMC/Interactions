package com.bluedragonmc.interactions.rules.misc

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.util.Tags
import com.bluedragonmc.interactions.util.getBlockOrAir
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace

/**
 * Farmland turns to dirt when the block above becomes solid and is not a crop that maintains it.
 */
class FarmlandRule : PlacementRule(BlockSet.from(Block.FARMLAND)) {
    init {
        onPlace {
            val above = instance.getBlockOrAir(blockPosition.relative(BlockFace.TOP))
            if (!canSurvive(above)) block = Block.DIRT
        }

        onUpdate {
            if (fromFace != BlockFace.TOP) return@onUpdate
            val above = instance.getBlockOrAir(blockPosition.relative(BlockFace.TOP))
            if (!canSurvive(above)) resultBlock = Block.DIRT
        }
    }

    private fun canSurvive(above: Block): Boolean =
        !above.solid() || above.key() in Tags.MAINTAINS_FARMLAND
}
