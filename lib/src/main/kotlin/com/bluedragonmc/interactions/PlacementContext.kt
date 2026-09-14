package com.bluedragonmc.interactions

import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace

/**
 * The context of a pending block placement, used to resolve where the block should actually be placed.
 *
 * @param placementPosition the position Minestom would place the block at by default (adjacent to [clickedBlock]).
 */
data class PlacementContext(
    val instance: Instance,
    val player: Player,
    val hand: PlayerHand,
    val block: Block,
    val blockFace: BlockFace,
    val cursorPosition: Point,
    val clickedPosition: BlockVec,
    val clickedBlock: Block,
    val placementPosition: BlockVec,
    val isSneaking: Boolean,
)
