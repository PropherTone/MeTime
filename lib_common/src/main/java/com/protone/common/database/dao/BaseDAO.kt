package com.protone.common.database.dao

import com.protone.common.database.MediaAction

abstract class BaseDAO<Action : MediaAction> {
    protected abstract fun sendEvent(mediaAction: Action)
}