package com.protone.common.utils.displayUtils.imageLoader

data class RequestFactory(
    var path: Any? = null,
    var type : ImageType = ImageType.Any,
    var requestInterceptor: RequestInterceptor? = null,
    var configMap: Map<String,Any>? = null
)
