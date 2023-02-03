package com.protone.common.baseType

inline fun <C> C.ifNotEmpty(defaultValue: (C) -> Unit) where C : Collection<*> {
    if (!isEmpty()) defaultValue(this)
}