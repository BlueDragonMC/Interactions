package com.bluedragonmc.interactions.rules

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.rules.base.FaceAttachedPlacementRule

class ButtonPlacementRule : FaceAttachedPlacementRule(BlockSet.fromTag("minecraft:buttons"))
