package com.protone.common.baseType

inline fun <C> C.ifNotEmpty(defaultValue: (C) -> Unit) where C : Collection<*> {
    if (!isEmpty()) defaultValue(this)
}

inline fun <T, K, V> Iterable<T>.associateToMapList(
    transformKey: (T) -> K,
    transformValue: (T) -> V
): Map<K, List<V>> {
    val map = mutableMapOf<K, MutableList<V>>()
    for (element in this) {
        val key = transformKey(element)
        val value = transformValue(element)
        (map[key] ?: run { mutableListOf<V>().also { map[key] = it } }).add(value)
    }
    return map
}