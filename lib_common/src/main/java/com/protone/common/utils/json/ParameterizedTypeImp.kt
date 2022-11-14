package com.protone.common.utils.json

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ParameterizedTypeImp(private val raw: Class<*>, val type: Array<Type>) : ParameterizedType {
    override fun getActualTypeArguments(): Array<Type> = type
    override fun getRawType(): Type = raw
    override fun getOwnerType(): Type? = null
}