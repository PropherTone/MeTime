package com.protone.base.view.behavior

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.protone.common.utils.TAG

class UserCardBehavior @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
): CoordinatorLayout.Behavior<RecyclerView>(context, attrs) {

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: RecyclerView,
        dependency: View
    ): Boolean {
        Log.d(TAG, "onDependentViewChanged: ${dependency.x}")
        dependency.x += 1
        return true
    }
}