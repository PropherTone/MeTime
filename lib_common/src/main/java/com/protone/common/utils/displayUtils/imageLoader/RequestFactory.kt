package com.protone.common.utils.displayUtils.imageLoader

data class RequestFactory(
    var path: Any? = null,
    var type : ImageType = ImageType.Any,
    var requestInterceptor: RequestInterceptor<LoadSuccessResult, LoadFailedResult>? = null,
    var size: ImageSize? = null,
    var enableDiskCache: Boolean = true,
    var enableMemoryCache: Boolean = true
)
