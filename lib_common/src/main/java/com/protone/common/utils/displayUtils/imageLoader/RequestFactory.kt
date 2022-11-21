package com.protone.common.utils.displayUtils.imageLoader

import java.util.ArrayDeque

internal data class RequestFactory(
    var path: Any? = null,
    var type : ImageType = ImageType.Any
)
