package com.bluedragonmc.interactions

import net.kyori.adventure.key.Key
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.event.trait.BlockEvent
import net.minestom.server.event.trait.InstanceEvent
import java.util.concurrent.atomic.AtomicInteger

/**
 * A right-click behavior for a set of blocks.
 *
 * Interaction rules are processed after placement rules, which lets a
 * placement rule claim an interaction first by setting
 * [PlayerBlockInteractEvent.isBlockingItemUse].
 */
abstract class BlockInteractionRule(val blocks: BlockSet) : Rule {

    /** Registry keys of every block covered by this rule. */
    override val keys: Set<Key> = blocks.keys

    /** The node for right-clicks on one of [blocks]. */
    internal val interactionNode: EventNode<BlockEvent> = EventNode.event(
        "interactions:block-interaction:${this::class.simpleName}#${COUNTER.incrementAndGet()}",
        EventFilter.BLOCK,
    ) { event -> event.block.key() in keys }

    /**
     * A second node for item-use events, which are not [BlockEvent]s and so cannot be received by
     * [interactionNode].
     */
    internal val useItemNode: EventNode<InstanceEvent> = EventNode.type(
        "interactions:block-use-item:${this::class.simpleName}#${COUNTER.incrementAndGet()}",
        EventFilter.INSTANCE,
    )

    init {
        interactionNode.priority = INTERACTION_PRIORITY
        useItemNode.priority = INTERACTION_PRIORITY
    }

    override val eventNodes: List<EventNode<out InstanceEvent>>
        get() = listOf(interactionNode, useItemNode)

    /** Called when a player right-clicks one of this rule's blocks. */
    protected fun onInteract(listener: PlayerBlockInteractEvent.() -> Unit) {
        interactionNode.addListener(PlayerBlockInteractEvent::class.java) { event -> listener(event) }
    }

    /** Called when one of this rule's blocks is broken. */
    protected fun onDestroy(listener: PlayerBlockBreakEvent.() -> Unit) {
        interactionNode.addListener(PlayerBlockBreakEvent::class.java) { event -> listener(event) }
    }

    /** Called when a player uses an item without targeting a block. */
    protected fun onUseItem(listener: PlayerUseItemEvent.() -> Unit) {
        useItemNode.addListener(PlayerUseItemEvent::class.java) { event -> listener(event) }
    }

    private companion object {
        /**
         * After the default priority (so placement rules handle an interaction first) but before the
         * global [InteractionRule]s, so block-specific interactions claim the click before generic
         * item interactions do.
         */
        const val INTERACTION_PRIORITY = 50
        val COUNTER = AtomicInteger()
    }
}
