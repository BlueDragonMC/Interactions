package com.bluedragonmc.interactions.util

import net.minestom.server.instance.Instance
import net.minestom.server.world.attribute.EnvironmentAttribute

/**
 * Reads an environment attribute by registry key, used for attributes Minestom models generically
 * (for example `minecraft:creaking_active`). Returns [fallback] when the attribute is unknown.
 */
@Suppress("UNCHECKED_CAST")
fun <T> Instance.environmentValue(key: String, fallback: T): T {
    val attribute = EnvironmentAttribute.values().firstOrNull { it.key().asString() == key } ?: return fallback
    val entry = getCachedDimensionType().attributes().entries()[attribute] ?: return fallback
    val modifier = entry.modifier() as EnvironmentAttribute.Modifier<T, Any?>
    return modifier.modify(attribute.defaultValue() as T, entry.argument())
}

/** Boolean variant of [environmentValue] by registry key. */
fun Instance.environmentBoolean(key: String, fallback: Boolean): Boolean =
    environmentValue(key, fallback)
