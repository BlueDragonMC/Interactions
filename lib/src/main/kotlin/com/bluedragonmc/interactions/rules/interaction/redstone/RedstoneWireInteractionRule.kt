package com.bluedragonmc.interactions.rules.interaction.redstone

import com.bluedragonmc.interactions.BlockInteractionRule
import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.util.RedstoneWire
import com.bluedragonmc.interactions.util.isSecondaryUseActive
import net.minestom.server.entity.GameMode
import net.minestom.server.instance.block.Block

/** Right-clicking on a redstone cross or dot re-shapes it. */
class RedstoneWireInteractionRule : BlockInteractionRule(BlockSet.from(Block.REDSTONE_WIRE)) {
    init {
        onInteract {
            if (isSecondaryUseActive()) return@onInteract
            if (player.gameMode == GameMode.ADVENTURE || player.gameMode == GameMode.SPECTATOR) return@onInteract
            if (!RedstoneWire.isCross(block) && !RedstoneWire.isDot(block)) return@onInteract

            val updated = RedstoneWire.withConnections(block, instance, blockPosition, RedstoneWire.isCross(block))
            if (updated == block) return@onInteract

            isBlockingItemUse = true
            instance.setBlock(blockPosition, updated)
        }
    }
}
