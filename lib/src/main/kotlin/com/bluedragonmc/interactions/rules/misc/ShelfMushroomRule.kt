package com.bluedragonmc.interactions.rules.misc

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.rules.base.HorizontalFacingRule
import net.minestom.server.instance.block.Block

/** Shelf mushrooms face the player and start at age 0. */
class ShelfMushroomRule : HorizontalFacingRule(BlockSet.from(Block.SHELF_MUSHROOM))
