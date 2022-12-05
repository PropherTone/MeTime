package com.protone.component.database.dao

import com.protone.component.database.MediaAction

abstract class BaseDAO<Action : MediaAction> {
    protected abstract suspend fun sendEvent(mediaAction: Action)
}