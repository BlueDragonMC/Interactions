package com.bluedragonmc.interactions

import net.kyori.adventure.key.Key
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.event.trait.BlockEvent
import net.minestom.server.event.trait.InstanceEvent
import java.util.concurrent.atomic.AtomicInteger

/**
 * A rule that reacts to interactions with any block, regardless of the clicked block's type.
 *
 * Interaction rules own no blocks: [keys] is empty, so they are never returned by
 * [RuleRegistry.ruleFor] and never conflict with block-keyed rules in a registry. They are
 * dispatched after placement rules (see [INTERACTION_PRIORITY]), which lets a placement rule claim an
 * interaction first by setting
 * [net.minestom.server.event.player.PlayerBlockInteractEvent.isBlockingItemUse].
 */
abstract class InteractionRule : Rule {

    override val keys: Set<Key> = emptySet()

    /** The node for right-clicks on a block. */
    internal val interactionNode: EventNode<BlockEvent> = EventNode.type(
        "interactions:interaction:${this::class.simpleName}#${COUNTER.incrementAndGet()}",
        EventFilter.BLOCK,
    )

    /**
     * A second node for item-use events, which are not [BlockEvent]s and so cannot be received by
     * [interactionNode].
     */
    internal val useItemNode: EventNode<InstanceEvent> = EventNode.type(
        "interactions:use-item:${this::class.simpleName}#${COUNTER.incrementAndGet()}",
        EventFilter.INSTANCE,
    )

    init {
        interactionNode.priority = INTERACTION_PRIORITY
        useItemNode.priority = INTERACTION_PRIORITY
    }

    override val eventNodes: List<EventNode<out InstanceEvent>>
        get() = listOf(interactionNode, useItemNode)

    /** Called when a player right-clicks a block, whatever the held item. */
    protected fun onInteract(listener: PlayerBlockInteractEvent.() -> Unit) {
        interactionNode.addListener(PlayerBlockInteractEvent::class.java) { event -> listener(event) }
    }

    /** Called when a player uses an item without targeting a block (for example, an empty bucket). */
    protected fun onUseItem(listener: PlayerUseItemEvent.() -> Unit) {
        useItemNode.addListener(PlayerUseItemEvent::class.java) { event -> listener(event) }
    }

    private companion object {
        /** Later than the default priority, so placement rules handle an interaction first. */
        const val INTERACTION_PRIORITY = 100
        val COUNTER = AtomicInteger()
    }
}
