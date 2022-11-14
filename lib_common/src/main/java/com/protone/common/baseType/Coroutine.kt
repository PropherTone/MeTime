package com.protone.common.baseType

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

inline fun CoroutineScope.launchDefault(crossinline func: suspend CoroutineScope.() -> Unit): Job =
    launch(Dispatchers.Default) {
        func.invoke(this)
    }

inline fun CoroutineScope.launchIO(crossinline func: suspend CoroutineScope.() -> Unit) : Job =
    launch(Dispatchers.IO) {
        func.invoke(this)
    }

inline fun CoroutineScope.launchMain(crossinline func: suspend CoroutineScope.() -> Unit) : Job =
    launch(Dispatchers.Main) {
        func.invoke(this)
    }

suspend inline fun <T> withMainContext(crossinline func: suspend CoroutineScope.() -> T) =
    withContext(Dispatchers.Main) {
        func.invoke(this)
    }

suspend inline fun <T> withIOContext(crossinline func: suspend CoroutineScope.() -> T) =
    withContext(Dispatchers.IO) {
        func.invoke(this)
    }

suspend inline fun <T> withDefaultContext(crossinline func: suspend CoroutineScope.() -> T) =
    withContext(Dispatchers.Default) {
        func.invoke(this)
    }

@Suppress("ObjectLiteralToLambda")
suspend inline fun <T> Flow<T>.bufferCollect(crossinline action: suspend (value: T) -> Unit) {
    buffer().collect(object : FlowCollector<T> {
        override suspend fun emit(value: T) = action(value)
    })
}