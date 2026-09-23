package com.bluedragonmc.interactions.rules.redstone

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.util.RedstoneWire
import com.bluedragonmc.interactions.util.States
import net.minestom.server.instance.block.Block

/** Redstone wire recomputes its connections and drops when it loses its support. */
class RedstoneWireRule : PlacementRule(BlockSet.from(Block.REDSTONE_WIRE)) {
    init {
        onPlace {
            if (!RedstoneWire.canSurvive(instance, blockPosition)) {
                isCancelled = true
                return@onPlace
            }
            block = RedstoneWire.withConnections(block, instance, blockPosition, false)
                .withProperty(States.POWER, "0")
        }

        onUpdate {
            if (!RedstoneWire.canSurvive(instance, blockPosition)) {
                destroy()
                return@onUpdate
            }
            resultBlock = RedstoneWire.withConnections(block, instance, blockPosition, RedstoneWire.isDot(block))
        }
    }
}
