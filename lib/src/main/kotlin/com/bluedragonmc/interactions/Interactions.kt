package com.bluedragonmc.interactions

import com.bluedragonmc.interactions.rules.ButtonPlacementRule
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode

class Interactions {
    private val interactionsEventNode: EventNode<Event> = EventNode.all("com.bluedragonmc.interactions-interactions")
    private val placementsEventNode: EventNode<Event> = EventNode.all("com.bluedragonmc.interactions-placements")
    private val allEventNode: EventNode<Event> = EventNode.all("com.bluedragonmc.interactions-all")

    init {
        allEventNode.addChild(interactionsEventNode)
        allEventNode.addChild(placementsEventNode)

        registerPlacement(ButtonPlacementRule())
    }

    private fun registerPlacement(behavior: BlockBehavior) {
        placementsEventNode.addChild(behavior.eventNode)
    }

    private fun registerInteraction(behavior: BlockBehavior) {
        interactionsEventNode.addChild(behavior.eventNode)
    }

    fun blockHandlers() = BlockHandlers.getAll()
    fun events() = allEventNode
    fun placementEvents() = placementsEventNode
    fun interactionsEvents() = interactionsEventNode
}
