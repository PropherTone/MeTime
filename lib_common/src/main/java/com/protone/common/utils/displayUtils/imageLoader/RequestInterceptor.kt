package com.protone.common.utils.displayUtils.imageLoader

abstract class RequestInterceptor<T : LoadSuccessResult,F : LoadFailedResult> {
    abstract fun onLoadSuccess(result: T)
    abstract fun onLoadFailed(result: F)
}