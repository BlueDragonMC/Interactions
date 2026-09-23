package com.bluedragonmc.interactions.util

import net.minestom.server.instance.block.BlockFace

/**
 * Property-name constants for the block-state properties used by the rules.
 *
 * Keeping these in one place avoids silent typos between a placement rule and its update handler.
 */
object States {
    const val FACING = "facing"
    const val FACE = "face"
    const val AXIS = "axis"
    const val HALF = "half"
    const val TYPE = "type"
    const val SHAPE = "shape"
    const val LAYERS = "layers"
    const val LEVEL = "level"
    const val WATERLOGGED = "waterlogged"
    const val HANGING = "hanging"
    const val POWERED = "powered"
    const val OPEN = "open"
    const val HINGE = "hinge"
    const val CANDLES = "candles"
    const val PICKLES = "pickles"
    const val EGGS = "eggs"
    const val FLOWER_AMOUNT = "flower_amount"
    const val SEGMENT_AMOUNT = "segment_amount"
    const val LIT = "lit"
    const val AGE = "age"
    const val TRIAL_SPAWNER_STATE = "trial_spawner_state"
    const val PART = "part"
    const val ROTATION = "rotation"
    const val ATTACHED = "attached"
    const val IN_WALL = "in_wall"
    const val ENABLED = "enabled"
    const val DISTANCE = "distance"
    const val POWER = "power"
    const val VERTICAL_DIRECTION = "vertical_direction"
    const val THICKNESS = "thickness"
    const val BOTTOM = "bottom"
    const val SNOWY = "snowy"
    const val BASE = "base"
    const val ORIENTATION = "orientation"
    const val CRAFTING = "crafting"
    const val TRIGGERED = "triggered"
    const val POTENT_SULFUR_STATE = "potent_sulfur_state"
    const val CREAKING_HEART_STATE = "creaking_heart_state"
    const val TIP = "tip"
    const val ATTACHMENT = "attachment"

    const val NORTH = "north"
    const val EAST = "east"
    const val SOUTH = "south"
    const val WEST = "west"
    const val UP = "up"
    const val DOWN = "down"

    /** The connection property name for [face] (`up`/`down` for vertical faces). */
    fun connection(face: BlockFace): String = when (face) {
        BlockFace.BOTTOM -> DOWN
        BlockFace.TOP -> UP
        BlockFace.NORTH -> NORTH
        BlockFace.SOUTH -> SOUTH
        BlockFace.WEST -> WEST
        BlockFace.EAST -> EAST
    }
}
