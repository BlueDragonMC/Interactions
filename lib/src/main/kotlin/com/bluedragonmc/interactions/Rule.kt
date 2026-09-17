package com.bluedragonmc.interactions

import net.kyori.adventure.key.Key
import net.minestom.server.event.EventNode
import net.minestom.server.event.trait.InstanceEvent

/**
 * A rule that can be registered into a [RuleRegistry].
 *
 * A rule declares the block registry [keys] it owns (empty when it owns no blocks) and every
 * [eventNodes] the registry should attach on its behalf.
 */
interface Rule {
    /** Registry keys of the blocks this rule owns, or an empty set when it owns none. */
    val keys: Set<Key>

    /** Every event node owned by this rule, all attached to the registry together. */
    val eventNodes: List<EventNode<out InstanceEvent>>
}
