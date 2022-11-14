package com.protone.base

import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel() {
    sealed class ViewEvent
}