package com.bluedragonmc.interactions

import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.event.trait.BlockEvent

abstract class BlockBehavior(val blocks: BlockSet) {
    val eventNode: EventNode<BlockEvent> = EventNode.event("$this", EventFilter.BLOCK) { event ->
        blocks.contains(event.block)
    }

    protected inline fun <reified E : BlockEvent> handle(crossinline func: E.() -> Unit) {
        eventNode.addListener(E::class.java) { event -> func(event) }
    }

    protected fun onPlace(func: PlayerBlockPlaceEvent.() -> Unit) = handle(func)
    protected fun onDestroy(func: PlayerBlockBreakEvent.() -> Unit) = handle(func)
    protected fun onInteract(func: PlayerBlockInteractEvent.() -> Unit) = handle(func)
}
