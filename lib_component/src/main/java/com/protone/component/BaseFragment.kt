package com.protone.component

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel

abstract class BaseFragment<ViewBinding : ViewDataBinding, VM : ViewModel> : Fragment() {

    protected lateinit var binding: ViewBinding
    protected lateinit var viewModel: VM

    abstract fun createViewModel(): VM

    abstract fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): ViewBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = createViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = createBinding(inflater, container, savedInstanceState).root

    fun startActivity(routerPath: String) {

    }
}