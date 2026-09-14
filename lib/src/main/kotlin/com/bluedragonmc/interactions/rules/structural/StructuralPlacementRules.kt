package com.bluedragonmc.interactions.rules.structural

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.rules.base.SlabRule
import com.bluedragonmc.interactions.rules.base.StairPlacementRule
import com.bluedragonmc.interactions.rules.base.TrapDoorPlacementRule

/** Slabs merge into the clicked slab and split `half` by the cursor. */
class SlabPlacementRule : SlabRule(BlockSet.fromTag("minecraft:slabs"))

/** Stairs face the player and recompute their `shape` from surrounding stairs. */
class StairsPlacementRule : StairPlacementRule(BlockSet.fromTag("minecraft:stairs"))

/** Trapdoors derive `facing`/`half` from the click and follow redstone signals. */
class TrapdoorRule : TrapDoorPlacementRule(BlockSet.fromTag("minecraft:trapdoors"))
