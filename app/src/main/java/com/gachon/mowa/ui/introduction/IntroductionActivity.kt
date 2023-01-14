package com.gachon.mowa.ui.introduction

import com.gachon.mowa.R
import com.gachon.mowa.base.BaseActivity
import com.gachon.mowa.databinding.ActivityIntroductionBinding
import com.gachon.mowa.ui.login.LoginActivity
import com.gachon.mowa.util.getGuideMode
import com.gachon.mowa.util.setGuideMode

class IntroductionActivity : BaseActivity<ActivityIntroductionBinding>(ActivityIntroductionBinding::inflate) {
    private var isShown = 1

    override fun initAfterBinding() {
        initFragment()
        initClickListener()
    }

    override fun onStart() {
        super.onStart()

        if (getGuideMode() == 0) {
            // 가이드 다시 보지 않기를 선택한 경우
            startNextActivityWithClear(LoginActivity::class.java)
        }
    }

    /**
     * 띄워 줄 프래그먼트를 초기화
     */
    private fun initFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.introduction_fl, IntroductionFragment())
            .commitAllowingStateLoss()
    }

    /**
     * 클릭 리스너를 초기화
     */
    private fun initClickListener() {
        // X 버튼 클릭 시
        binding.introductionExitIv.setOnClickListener {
            close()
        }
    }

    /**
     * 해당 가이드를 닫아줍니다.
     */
    private fun close() {
        isShown = if (binding.introductionNotShowCb.isChecked) 0 else 1
        setGuideMode(isShown)
        startNextActivityWithClear(LoginActivity::class.java)
    }
}