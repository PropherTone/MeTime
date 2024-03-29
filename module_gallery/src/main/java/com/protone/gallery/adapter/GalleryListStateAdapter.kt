package com.protone.gallery.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.protone.gallery.fragment.GalleryListFragment

class GalleryListStateAdapter(
    context: FragmentActivity,
    private val fragments: List<GalleryListFragment>
) : FragmentStateAdapter(context) {
    override fun createFragment(position: Int): Fragment = fragments[position]
    override fun getItemCount(): Int = fragments.size
}