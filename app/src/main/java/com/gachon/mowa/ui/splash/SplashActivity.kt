package com.gachon.mowa.ui.splash

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.View
import com.gachon.mowa.base.BaseActivity
import com.gachon.mowa.databinding.ActivitySplashBinding
import com.gachon.mowa.ui.introduction.IntroductionActivity
import com.gachon.mowa.ui.login.LoginActivity
import com.gachon.mowa.ui.login.LoginLargeActivity
import com.gachon.mowa.ui.main.MainActivity
import com.gachon.mowa.util.getGuideMode
import com.gachon.mowa.util.getScreenMode
import com.gachon.mowa.util.getUserId

/**
 * Activity - Splash
 *
 * -> IntroActivity (if the user logged out or has never logged in)
 * -> MainActivity (if the user is already logged in)
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {

    // onCreate 이후 해줄 작업을 아래에 추가해주면 됩니다.
    override fun initAfterBinding() {
        val isGuideShown = getGuideMode()

        if (isGuideShown == 1) {
            startNextActivity(IntroductionActivity::class.java)
            return
        }

        // Splash 보이도록
        binding.splashBgV.visibility = View.VISIBLE
        binding.splashLogoIv.visibility = View.VISIBLE

        // 2초 뒤에 MainActivity를 띄어줍니다.
        Handler(Looper.getMainLooper()).postDelayed({
            if (getScreenMode() == 0) {
                // Screen mode 0일 경우 (default)
                if (getUserId().equals("")) {
                    startNextActivityWithClear(LoginActivity::class.java)
                } else {
                    startNextActivityWithClear(MainActivity::class.java)
                }

            } else {
                // Screen mode 1일 경우 (large)
                if (getUserId().equals("")) {
                    startNextActivityWithClear(LoginLargeActivity::class.java)
                } else {
                    startNextActivityWithClear(MainActivity::class.java)
                }
            }

        }, 2000)
    }
}
