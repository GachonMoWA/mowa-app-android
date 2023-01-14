package com.gachon.mowa.util

/**
 * User id 정보를 SharedPreferences에 저장합니다.
 */
fun setUserId(userId: String) {
    val editor = ApplicationClass.mSharedPreferences.edit()
    editor.putString(ApplicationClass.TAG_USER_ID, userId)
    editor.apply()
}

/**
 * User id 정보를 SharedPreferences로부터 가져옵니다.
 */
fun getUserId(): String? =
    ApplicationClass.mSharedPreferences.getString(ApplicationClass.TAG_USER_ID, "")

/**
 * User email 정보를 SharedPreferences에 저장합니다.
 */
fun setUserEmail(userEmail: String) {
    val editor = ApplicationClass.mSharedPreferences.edit()
    editor.putString(ApplicationClass.TAG_USER_EMAIL, userEmail)
    editor.apply()
}

/**
 * User email 정보를 SharedPreferences로부터 가져옵니다.
 */
fun getUserEmail(): String? =
    ApplicationClass.mSharedPreferences.getString(ApplicationClass.TAG_USER_EMAIL, "")


/**
 * Screen mode 값을 SharedPreferences에 저장합니다.
 * 0: 디폴트 모드
 * 1: 확대 모드
 */
fun setScreenMode(screenMode: Int) {
    val editor = ApplicationClass.mSharedPreferences.edit()
    editor.putInt(ApplicationClass.TAG_SCREEN_MODE, screenMode)
    editor.apply()
}

/**
 * Screen mode 값을 SharedPreferences로부터 가져옵니다.
 */
fun getScreenMode(): Int =
    ApplicationClass.mSharedPreferences.getInt(ApplicationClass.TAG_SCREEN_MODE, 0)

/**
 * Guide 다시 보기 체크 여부를 SharedPreferences에 저장합니다.
 * 0: 다시 보지 않기
 * 1: 다시 보기
 */
fun setGuideMode(guideMode: Int) {
    val editor = ApplicationClass.mSharedPreferences.edit()
    editor.putInt(ApplicationClass.TAG_GUIDE_MODE, guideMode)
    editor.apply()
}

/**
 * Guide 다시 보기 체크 여부를 SharedPreferences로부터 가져옵니다.
 */
fun getGuideMode(): Int =
    ApplicationClass.mSharedPreferences.getInt(ApplicationClass.TAG_GUIDE_MODE, 1)
