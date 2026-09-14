package com.bluedragonmc.interactions.rules.redstone

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.rules.base.HorizontalFacingRule
import com.bluedragonmc.interactions.rules.base.PressurePlatePlacementRule
import com.bluedragonmc.interactions.rules.base.RailPlacementRule
import net.minestom.server.instance.block.Block

/** Wooden and weighted pressure plates sit on a supporting block. */
class PressurePlateRule : PressurePlatePlacementRule(BlockSet.fromTag("minecraft:pressure_plates"))

/** Rails run along the player's horizontal look. */
class RailRule : RailPlacementRule(BlockSet.fromTag("minecraft:rails"))

/** Repeaters face away from the player. */
class RepeaterRule : HorizontalFacingRule(BlockSet.from(Block.REPEATER))

/** Comparators face away from the player. */
class ComparatorRule : HorizontalFacingRule(BlockSet.from(Block.COMPARATOR))
