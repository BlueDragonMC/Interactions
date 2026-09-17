package com.bluedragonmc.interactions

import com.bluedragonmc.interactions.internal.PlacementHandler
import net.kyori.adventure.key.Key
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.trait.InstanceEvent
import net.minestom.server.instance.block.Block

/**
 * Holds a set of [PlacementRule]s and exposes a single event node that activates them.
 */
class PlacementRuleRegistry {
    private val root: EventNode<InstanceEvent> = EventNode.type("interactions:registry", EventFilter.INSTANCE)
    private val rulesByKey = mutableMapOf<Key, PlacementRule>()
    private val ruleList = mutableListOf<PlacementRule>()

    init {
        root.addChild(PlacementHandler(::ruleFor).node)
    }

    /**
     * Registers [rule] and makes its event node a child of this registry's node.
     *
     * @throws IllegalStateException if two rules in this registry cover the same block.
     */
    fun register(rule: PlacementRule) {
        val conflicts = rule.keys.filter { it in rulesByKey }
        check(conflicts.isEmpty()) {
            val owners = conflicts.joinToString { "$it (${rulesByKey[it]?.let { r -> r::class.simpleName }})" }
            "Block(s) already have a placement rule in this registry: $owners"
        }
        rule.keys.forEach { rulesByKey[it] = rule }
        ruleList += rule
        rule.eventNodes.forEach { root.addChild(it) }
    }

    /** Removes [rule] and all of its event listeners from this registry. */
    fun unregister(rule: PlacementRule) {
        if (!ruleList.remove(rule)) return
        rule.keys.forEach { rulesByKey.remove(it) }
        rule.eventNodes.forEach { root.removeChild(it) }
    }

    /** The rule responsible for [block], or null if this registry has no rule for it. */
    fun ruleFor(block: Block): PlacementRule? = rulesByKey[block.key()]

    /** A snapshot of the registered rules. */
    fun rules(): List<PlacementRule> = ruleList.toList()

    /** Attach this node to an instance's event node or the global event handler to activate the registry. */
    fun eventNode(): EventNode<InstanceEvent> = root
}
