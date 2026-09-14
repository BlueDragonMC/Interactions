package com.bluedragonmc.interactions.event

import net.minestom.server.coordinate.BlockVec
import net.minestom.server.event.trait.BlockEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block

/**
 * Dispatched when a block is selected by the random tick system.
 *
 * Note: the random tick generator is not implemented yet, so this event is currently never dispatched.
 * The listener hook exists so rules can opt in once a generator is added.
 */
class RandomTickEvent(
    instance: Instance,
    position: BlockVec,
    block: Block,
) : BlockEvent {

    private val instanceValue: Instance = instance
    private val blockPositionValue: BlockVec = position
    private val blockValue: Block = block

    /** The block to replace the current block with. Defaults to no change. */
    var resultBlock: Block = block

    override fun getInstance(): Instance = instanceValue

    override fun getBlock(): Block = blockValue

    override fun getBlockPosition(): BlockVec = blockPositionValue
}
