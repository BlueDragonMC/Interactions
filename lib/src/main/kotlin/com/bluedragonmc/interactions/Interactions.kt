package com.bluedragonmc.interactions

import net.minestom.server.event.Event
import net.minestom.server.event.EventNode

object Interactions {
    @JvmStatic
    fun getBlockHandlers() = BlockHandlers.getAll()

    private val interactionsEventNode: EventNode<Event> = EventNode.all("com.bluedragonmc.interactions-interactions")
    private val placementsEventNode: EventNode<Event> = EventNode.all("com.bluedragonmc.interactions-placements")
    private val allEventNode: EventNode<Event> = EventNode.all("com.bluedragonmc.interactions-all")

    init {
        allEventNode.addChild(interactionsEventNode)
        allEventNode.addChild(placementsEventNode)
    }

    fun events() = allEventNode
    fun placementEvents() = placementsEventNode
    fun interactionsEvents() = interactionsEventNode
}
