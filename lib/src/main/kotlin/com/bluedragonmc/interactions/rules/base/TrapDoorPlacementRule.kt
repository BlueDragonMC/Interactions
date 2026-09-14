package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.util.Signals
import com.bluedragonmc.interactions.util.States
import com.bluedragonmc.interactions.util.Waterlogged
import com.bluedragonmc.interactions.util.horizontalDirection
import net.minestom.server.instance.block.BlockFace

/**
 * Base for trapdoors: `facing`/`half` come from the click, while `open`/`powered` follow neighbor
 * signals and toggle as the signal changes.
 */
abstract class TrapDoorPlacementRule(blocks: BlockSet) : PlacementRule(blocks) {

    init {
        onPlace {
            val replacing = context.placementPosition == context.clickedPosition
            val clickedHorizontal = blockFace.toDirection().horizontal()
            val state = if (!replacing && clickedHorizontal) {
                block
                    .withProperty(States.FACING, blockFace.toDirection().name.lowercase())
                    .withProperty(States.HALF, if (cursorPosition.y() > 0.5) "top" else "bottom")
            } else {
                val facing = player.position.horizontalDirection().oppositeFace
                block
                    .withProperty(States.FACING, facing.name.lowercase())
                    .withProperty(States.HALF, if (blockFace == BlockFace.TOP) "bottom" else "top")
            }
            val powered = Signals.hasNeighborSignal(instance, blockPosition)
            block = Waterlogged.apply(
                state
                    .withProperty(States.OPEN, powered.toString())
                    .withProperty(States.POWERED, powered.toString()),
                instance,
                blockPosition,
            )
        }

        onUpdate {
            val signal = Signals.hasNeighborSignal(instance, blockPosition)
            if (signal == (block.getProperty(States.POWERED) == "true")) return@onUpdate
            resultBlock = block
                .withProperty(States.OPEN, signal.toString())
                .withProperty(States.POWERED, signal.toString())
        }
    }
}
