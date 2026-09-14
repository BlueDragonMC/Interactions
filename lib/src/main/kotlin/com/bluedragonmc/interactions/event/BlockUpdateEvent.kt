package com.bluedragonmc.interactions.event

import net.minestom.server.coordinate.BlockVec
import net.minestom.server.event.trait.BlockEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace

/**
 * Dispatched synchronously when a neighboring block changes and this block's state may need to change.
 *
 * The listener should write the updated state to [resultBlock]; the library applies it and continues
 * propagating updates outward.
 *
 * @param fromFace the face of this block that points toward the changed neighbor.
 */
class BlockUpdateEvent(
    instance: Instance,
    position: BlockVec,
    block: Block,
    val fromFace: BlockFace,
) : BlockEvent {

    private val instanceValue: Instance = instance
    private val blockPositionValue: BlockVec = position
    private val blockValue: Block = block

    /** The block to replace the current block with. Defaults to no change. */
    var resultBlock: Block = block

    override fun getInstance(): Instance = instanceValue

    override fun getBlock(): Block = blockValue

    override fun getBlockPosition(): BlockVec = blockPositionValue

    /** Removes this block as a result of the update. */
    fun destroy() {
        resultBlock = Block.AIR
    }
}
