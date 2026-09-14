package com.bluedragonmc.interactions.rules

import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.PlacementRuleRegistry

/**
 * Every rule shipped by this library, and the entry point for registering all of them at once.
 *
 * ```kotlin
 * val registry = PlacementRuleRegistry()
 * RuleCatalog.registerAll(registry)
 * MinecraftServer.getGlobalEventHandler().addChild(registry.eventNode())
 * ```
 */
object RuleCatalog {

    /** Creates a fresh list of every rule shipped by the library. */
    fun createRules(): List<PlacementRule> = listOf(
        ButtonPlacementRule(),
    )

    /** Creates and registers every rule in this catalog into [registry]. */
    fun registerAll(registry: PlacementRuleRegistry) {
        createRules().forEach(registry::register)
    }
}
