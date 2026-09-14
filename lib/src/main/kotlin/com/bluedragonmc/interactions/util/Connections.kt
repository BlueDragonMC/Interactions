package com.bluedragonmc.interactions.util

import net.minestom.server.instance.block.Block

/** Helpers used by the connecting rules (fences, walls, panes, bars). */
object Connections {

    fun isFenceGate(block: Block): Boolean = block.key().asString().endsWith("_fence_gate")

    fun isIronBarsOrPane(block: Block): Boolean {
        val key = block.key().asString()
        return key == "minecraft:iron_bars" || key.endsWith("_glass_pane")
    }

    fun isWall(block: Block): Boolean = block.key() in Tags.WALLS
}
