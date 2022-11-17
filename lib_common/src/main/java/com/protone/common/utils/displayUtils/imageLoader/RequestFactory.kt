package com.protone.common.utils.displayUtils.imageLoader

import com.protone.common.utils.displayUtils.imageLoader.constant.ConfigConstant
import java.util.ArrayDeque

internal data class RequestFactory(
    var path: Any? = null,
    var type : ImageType = ImageType.Any,
    var requestInterceptor: RequestInterceptor? = null,
    var configs: ArrayDeque<ConfigConstant>? = null
)
