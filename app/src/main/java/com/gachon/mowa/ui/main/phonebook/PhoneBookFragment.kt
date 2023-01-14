package com.gachon.mowa.ui.main.phonebook

import com.gachon.mowa.base.BaseFragment
import com.gachon.mowa.databinding.FragmentPhoneBookBinding
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Telephone book
 */
class PhoneBookFragment :
    BaseFragment<FragmentPhoneBookBinding>(FragmentPhoneBookBinding::inflate) {
    companion object {
        const val TAG = "FRAG/PHONEBOOK"
    }

    private val tabMenu = arrayListOf("주변 복지관", "전화번호부", "비상연락처")

    private lateinit var phoneBookVPAdapter: PhoneBookVPAdapter

    override fun initAfterBinding() {
    }

    override fun onResume() {
        super.onResume()
        initViewPager()
    }

    /**
     * ViewPager를 초기화
     */
    private fun initViewPager() {
        // 뷰페이저 어댑터를 연결
        phoneBookVPAdapter = PhoneBookVPAdapter(this)
        binding.phoneBookVp.adapter = phoneBookVPAdapter

        // TabLayout과 뷰페이저를 연결
        TabLayoutMediator(binding.phoneBookTl, binding.phoneBookVp) { tab, position ->
            tab.text = tabMenu[position]
        }.attach()
    }
}
