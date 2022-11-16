package com.protone.common.utils.displayUtils.imageLoader

abstract class RequestInterceptor {
    abstract fun onLoadSuccess(result: LoadSuccessResult)
    abstract fun onLoadFailed(result: LoadFailedResult)
}