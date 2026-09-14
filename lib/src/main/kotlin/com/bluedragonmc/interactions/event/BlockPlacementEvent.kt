package com.bluedragonmc.interactions.event

import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.event.trait.BlockEvent
import net.minestom.server.event.trait.CancellableEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace

/**
 * Dispatched before a block is placed, with the placement position already resolved by the library.
 *
 * Unlike [net.minestom.server.event.player.PlayerBlockPlaceEvent], this event exposes both the position
 * the player clicked and the position the block will actually occupy, and is not coupled to Minestom's
 * placement-rule based position resolution.
 *
 * Listeners mutate the returned block to change the placed block, and set [isCancelled] to abort the placement.
 */
class BlockPlacementEvent(
    val player: Player,
    instance: Instance,
    val hand: PlayerHand,
    /** The face of [clickedBlock] that the player clicked. */
    val blockFace: BlockFace,
    /** Cursor position on the clicked block, in the range [0, 1]. */
    val cursorPosition: Point,
    /** Position of the block the player clicked. */
    val clickedPosition: BlockVec,
    /** The block the player clicked. */
    val clickedBlock: Block,
    position: BlockVec,
    block: Block,
    consumeBlock: Boolean,
) : BlockEvent, CancellableEvent {

    private val instanceValue: Instance = instance
    private val blockPositionValue: BlockVec = position
    private var blockValue: Block = block
    private var cancelledValue: Boolean = false

    /** Whether the held item should be consumed when this placement succeeds. */
    var consumeBlock: Boolean = consumeBlock

    private val extraPlacements = mutableListOf<Pair<BlockVec, Block>>()

    override fun getInstance(): Instance = instanceValue

    override fun getBlock(): Block = blockValue

    override fun getBlockPosition(): BlockVec = blockPositionValue

    override fun isCancelled(): Boolean = cancelledValue

    override fun setCancelled(cancel: Boolean) {
        cancelledValue = cancel
    }

    /** Replaces the block that will be placed. */
    fun setBlock(block: Block) {
        blockValue = block
    }

    /**
     * Places an additional block at [position] as part of this placement, such as the second half of a door.
     * Replaces whatever is currently at that position.
     */
    fun placeAt(position: BlockVec, block: Block) {
        extraPlacements += position to block
    }

    internal fun extraPlacements(): List<Pair<BlockVec, Block>> = extraPlacements
}
