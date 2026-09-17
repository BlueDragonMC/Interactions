package com.bluedragonmc.interactions.rules.interaction

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.util.Fire
import com.bluedragonmc.interactions.util.States
import net.minestom.server.instance.block.Block

/**
 * Fire recomputes its connection properties when a neighbor changes, and is removed when it loses
 * all support. Its `age` is preserved.
 */
class FireRule : PlacementRule(BlockSet.from(Block.FIRE, Block.SOUL_FIRE)) {
    init {
        onUpdate {
            val age = block.getProperty(States.AGE) ?: "0"
            val updated = Fire.stateAt(instance, blockPosition, age)
            if (updated == null) {
                destroy()
            } else if (updated != block) {
                resultBlock = updated
            }
        }
    }
}
