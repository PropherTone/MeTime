package com.protone.common.utils.displayUtils.imageLoader

import com.protone.common.utils.displayUtils.imageLoader.LoadFailedResult
import com.protone.common.utils.displayUtils.imageLoader.LoadSuccessResult

interface Interceptor {
    fun onLoadSuccess(result: LoadSuccessResult)
    fun onLoadFailed(result: LoadFailedResult)
}