package com.bluedragonmc.interactions

import com.bluedragonmc.interactions.event.BlockPlacementEvent
import com.bluedragonmc.interactions.event.BlockUpdateEvent
import com.bluedragonmc.interactions.event.RandomTickEvent
import net.kyori.adventure.key.Key
import net.minestom.server.coordinate.Point
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.trait.BlockEvent
import net.minestom.server.event.trait.InstanceEvent
import net.minestom.server.instance.block.rule.BlockPlacementRule
import java.util.concurrent.atomic.AtomicInteger

/**
 * Behavior for a set of blocks, expressed entirely as event listeners.
 *
 * Rules never touch Minestom's block placement-rule or block-handler registries. Instead, they expose
 * [eventNodes], which are aggregated by a [PlacementRuleRegistry] and ultimately attached to an
 * instance or the global event handler.
 *
 * Matching is done by block registry key, so a rule for [BlockSet.fromTag] `minecraft:buttons` matches
 * a button regardless of its properties.
 */
abstract class PlacementRule(val blocks: BlockSet) {

    /** Registry keys of every block covered by this rule. */
    val keys: Set<Key> = blocks.keys

    /**
     * The rule's own event node for block events, owned by its [PlacementRuleRegistry].
     */
    internal val defaultEventNode: EventNode<BlockEvent> = EventNode.event(
        "interactions:rule:${this::class.simpleName}#${COUNTER.incrementAndGet()}",
        EventFilter.BLOCK,
    ) { event -> matches(event) }

    /**
     * Every event node owned by this rule, all attached to the registry together.
     *
     * Most rules only need [defaultEventNode]. Rules that also react to non-block events, such as item
     * use, add their extra nodes to this list.
     */
    internal open val eventNodes: List<EventNode<out InstanceEvent>> get() = listOf(defaultEventNode)

    /**
     * Whether this rule should receive the [event].
     *
     * Block rules match on the event's block key. [InteractionRule] overrides this to react to
     * interactions with any block.
     */
    protected open fun matches(event: BlockEvent): Boolean = event.block.key() in keys

    /** Called when a player places one of this rule's blocks. */
    protected fun onPlace(listener: BlockPlacementEvent.() -> Unit) {
        defaultEventNode.addListener(BlockPlacementEvent::class.java) { event -> listener(event) }
    }

    /** Called when one of this rule's blocks is broken. */
    protected fun onDestroy(listener: PlayerBlockBreakEvent.() -> Unit) {
        defaultEventNode.addListener(PlayerBlockBreakEvent::class.java) { event -> listener(event) }
    }

    /** Called when a player right-clicks one of this rule's blocks. */
    protected fun onInteract(listener: PlayerBlockInteractEvent.() -> Unit) {
        defaultEventNode.addListener(PlayerBlockInteractEvent::class.java) { event -> listener(event) }
    }

    /** Called when a neighboring block changes and this rule's block may need to update. */
    protected fun onUpdate(listener: BlockUpdateEvent.() -> Unit) {
        defaultEventNode.addListener(BlockUpdateEvent::class.java) { event -> listener(event) }
    }

    /**
     * Called when one of this rule's blocks is selected by the random tick system.
     *
     * Note: the random tick generator is not implemented yet, so this listener is currently never invoked.
     */
    protected fun onRandomTick(listener: RandomTickEvent.() -> Unit) {
        defaultEventNode.addListener(RandomTickEvent::class.java) { event -> listener(event) }
    }

    /**
     * Resolves the position this rule's block should be placed at.
     *
     * The default returns Minestom's resolved position, which places the block adjacent to the clicked
     * block. Rules that merge into the clicked block (slabs, snow layers, ...) should return
     * [PlacementContext.clickedPosition] when appropriate.
     */
    open fun getPlacementPosition(context: PlacementContext): Point = context.placementPosition

    /**
     * Whether this rule can handle a click that Minestom refuses to turn into a placement event because
     * the resolved position is already occupied by another block.
     *
     * This allows blocks like slabs, snow layers, candles, sea pickles, turtle eggs and flower beds grow into
     * an existing block of the same type. See [com.bluedragonmc.interactions.rules.base.StackingPlacementRule].
     */
    open fun canHandleOccupiedPlacement(context: PlacementContext): Boolean = false

    /**
     * Maximum distance, in blocks, that an update originating from this rule may propagate.
     */
    open fun maxUpdateDistance(): Int = BlockPlacementRule.DEFAULT_UPDATE_RANGE

    private companion object {
        val COUNTER = AtomicInteger()
    }
}
