package com.bluedragonmc.interactions.util

import net.minestom.server.instance.Instance
import net.minestom.server.world.attribute.EnvironmentAttribute

/**
 * Reads a dimension environment attribute (for example `minecraft:bed_rule`) from [instance]'s
 * cached dimension type, returning [fallback] when the dimension does not override it.
 */
@Suppress("UNCHECKED_CAST")
fun <T> environmentValue(instance: Instance, attribute: EnvironmentAttribute<T>, fallback: T): T {
    val entry = instance.cachedDimensionType.attributes().entries()[attribute] ?: return fallback
    val modifier = entry.modifier() as EnvironmentAttribute.Modifier<T, Any?>
    return modifier.modify(attribute.defaultValue(), entry.argument())
}

/**
 * Reads an environment attribute by registry key, used for attributes Minestom models generically
 * (for example `minecraft:creaking_active`). Returns [fallback] when the attribute is unknown.
 */
@Suppress("UNCHECKED_CAST")
fun <T> environmentValue(instance: Instance, key: String, fallback: T): T {
    val attribute = EnvironmentAttribute.values().firstOrNull { it.key().asString() == key } ?: return fallback
    val entry = instance.cachedDimensionType.attributes().entries()[attribute] ?: return fallback
    val modifier = entry.modifier() as EnvironmentAttribute.Modifier<T, Any?>
    return modifier.modify(attribute.defaultValue() as T, entry.argument())
}

/** Boolean variant of [environmentValue] by registry key. */
fun Instance.environmentBoolean(key: String, fallback: Boolean): Boolean =
    environmentValue(this, key, fallback)
