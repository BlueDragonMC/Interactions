package com.bluedragonmc.interactions.rules.wall

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.rules.base.FaceAttachedPlacementRule
import com.bluedragonmc.interactions.rules.base.HangingPlacementRule
import com.bluedragonmc.interactions.rules.base.WallTorchPlacementRule
import com.bluedragonmc.interactions.util.States
import net.minestom.server.instance.block.Block

/** Levers mount on a face and toggle their `powered` state when used. */
class LeverRule : FaceAttachedPlacementRule(BlockSet.from(Block.LEVER)) {
    init {
        onInteract {
            val powered = block.getProperty(States.POWERED) == "true"
            instance.setBlock(blockPosition, block.withProperty(States.POWERED, (!powered).toString()))
        }
    }
}

/** Lanterns hang below a support or sit on top of one. */
class LanternRule : HangingPlacementRule(BlockSet.fromTag("minecraft:lanterns"))

/** Ladders mount on a wall and point away from their support. */
class LadderRule : WallTorchPlacementRule(BlockSet.from(Block.LADDER))
