package com.bluedragonmc.interactions

import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.event.trait.BlockEvent
import net.minestom.server.event.trait.InstanceEvent
import java.util.concurrent.atomic.AtomicInteger

/**
 * A rule that reacts to interactions with any block, regardless of the clicked block's type.
 *
 * Interaction rules never own a block's placement: their [blocks] set is empty, so they are never
 * returned by [PlacementRuleRegistry.ruleFor] and never conflict with block-keyed rules in a registry.
 * They are dispatched after block-keyed rules (see [INTERACTION_PRIORITY]), which lets a specific rule
 * claim an interaction first by setting
 * [net.minestom.server.event.player.PlayerBlockInteractEvent.isBlockingItemUse].
 */
abstract class InteractionRule : PlacementRule(BlockSet.from()) {

    /**
     * A second node for item-use events, which are not [BlockEvent]s and so cannot be received by
     * [defaultEventNode]. Used by [onUseItem].
     */
    internal val useItemNode: EventNode<InstanceEvent> = EventNode.type(
        "interactions:use-item:${this::class.simpleName}#${COUNTER.incrementAndGet()}",
        EventFilter.INSTANCE,
    )

    init {
        defaultEventNode.priority = INTERACTION_PRIORITY
        useItemNode.priority = INTERACTION_PRIORITY
    }

    override val eventNodes: List<EventNode<out InstanceEvent>>
        get() = listOf(defaultEventNode, useItemNode)

    final override fun matches(event: BlockEvent): Boolean = true

    /** Called when a player uses an item without targeting a block (for example, an empty bucket). */
    protected fun onUseItem(listener: PlayerUseItemEvent.() -> Unit) {
        useItemNode.addListener(PlayerUseItemEvent::class.java) { event -> listener(event) }
    }

    private companion object {
        /** Later than the default priority, so block-keyed rules handle an interaction first. */
        const val INTERACTION_PRIORITY = 100
        val COUNTER = AtomicInteger()
    }
}
