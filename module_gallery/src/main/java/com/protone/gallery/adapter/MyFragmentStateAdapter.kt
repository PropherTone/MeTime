package com.protone.gallery.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MyFragmentStateAdapter(context: FragmentActivity, private val fragments: List<Fragment>) :
    FragmentStateAdapter(context) {
    override fun createFragment(position: Int): Fragment = fragments[position]
    override fun getItemCount(): Int = fragments.size
    fun getFragment(position: Int) = if (position >= fragments.size) null else fragments[position]
}