package com.gachon.mowa.ui.introduction

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class IntroductionVPAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private val fragments: ArrayList<Fragment> = ArrayList()

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    fun addFragment(fragment: Fragment) {
        fragments.add(fragment)

        // 추가된 곳의 인덱스를 뷰페이저에게 알려줍니다.
        notifyItemInserted(fragments.size - 1)
    }
}
