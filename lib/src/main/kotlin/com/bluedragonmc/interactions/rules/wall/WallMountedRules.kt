package com.bluedragonmc.interactions.rules.wall

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.rules.base.FaceAttachedPlacementRule
import com.bluedragonmc.interactions.rules.base.HangingPlacementRule
import com.bluedragonmc.interactions.rules.base.WallTorchPlacementRule
import net.minestom.server.instance.block.Block

/** Levers mount on a face. */
class LeverRule : FaceAttachedPlacementRule(BlockSet.from(Block.LEVER))

/** Lanterns hang below a support or sit on top of one. */
class LanternRule : HangingPlacementRule(BlockSet.fromTag("minecraft:lanterns"))

/** Ladders mount on a wall and point away from their support. */
class LadderRule : WallTorchPlacementRule(BlockSet.from(Block.LADDER))
