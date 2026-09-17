package com.bluedragonmc.interactions.rules

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.rules.base.FaceAttachedPlacementRule
import com.bluedragonmc.interactions.util.isSecondaryUseActive

class ButtonPlacementRule : FaceAttachedPlacementRule(BlockSet.fromTag("minecraft:buttons")) {
    init {
        // Pressing a button must not also place the block the player is holding.
        onInteract {
            if (isSecondaryUseActive()) return@onInteract
            isBlockingItemUse = true
        }
    }
}
