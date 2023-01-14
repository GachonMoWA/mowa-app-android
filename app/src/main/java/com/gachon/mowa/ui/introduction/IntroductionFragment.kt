package com.gachon.mowa.ui.introduction

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.gachon.mowa.base.BaseFragment
import com.gachon.mowa.databinding.FragmentIntroductionBinding
import com.gachon.mowa.ui.introduction.content.Introduction1Fragment
import com.gachon.mowa.ui.introduction.content.Introduction2Fragment
import com.gachon.mowa.ui.introduction.content.Introduction3Fragment

class IntroductionFragment :
    BaseFragment<FragmentIntroductionBinding>(FragmentIntroductionBinding::inflate) {

    override fun initAfterBinding() {
        initViewPager()
    }

    /**
     * ViewPager를 초기화
     */
    private fun initViewPager() {
        val introductionVPAdapter: IntroductionVPAdapter = IntroductionVPAdapter(this)

        // fragment 추가하기
        introductionVPAdapter.addFragment(Introduction1Fragment())
        introductionVPAdapter.addFragment(Introduction2Fragment())
        introductionVPAdapter.addFragment(Introduction3Fragment())

        binding.introductionVp.adapter = introductionVPAdapter
        binding.introductionVp.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.introductionIndicator.setViewPager2(binding.introductionVp)

        // 스크롤 그림자를 없애줍니다.
        binding.introductionVp.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    }
}
