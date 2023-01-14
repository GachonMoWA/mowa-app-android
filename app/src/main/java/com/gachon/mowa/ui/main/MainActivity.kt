package com.gachon.mowa.ui.main

import ai.api.AIConfiguration
import ai.api.AIDataService
import ai.api.model.AIRequest
import android.Manifest
import android.content.Context
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import com.gachon.mowa.R
import com.gachon.mowa.base.BaseActivity
import com.gachon.mowa.data.local.AppDatabase
import com.gachon.mowa.databinding.ActivityMainBinding
import com.gachon.mowa.ui.account.AccountActivity
import com.gachon.mowa.ui.guide.GuideActivity
import com.gachon.mowa.ui.introduction.IntroductionActivity
import com.gachon.mowa.ui.login.LoginActivity
import com.gachon.mowa.ui.main.home.HomeFragment
import com.gachon.mowa.ui.main.speaker.SpeakerFragment
import com.gachon.mowa.ui.main.phonebook.PhoneBookFragment
import com.gachon.mowa.ui.policy.PolicyActivity
import com.gachon.mowa.ui.sensor.SensorActivity
import com.gachon.mowa.util.ApplicationClass
import com.gachon.mowa.util.ApplicationClass.Companion.PERMISSIONS_REQUEST_READ_LOCATION
import com.gachon.mowa.util.ApplicationClass.Companion.TIME_PICKER_INTERVAL
import com.gachon.mowa.util.ApplicationClass.Companion.credentials
import com.gachon.mowa.util.ApplicationClass.Companion.currentWindowMetricsPointCompat
import com.gachon.mowa.util.ApplicationClass.Companion.endHour
import com.gachon.mowa.util.ApplicationClass.Companion.endMinute
import com.gachon.mowa.util.ApplicationClass.Companion.getNotificationIntent
import com.gachon.mowa.util.ApplicationClass.Companion.sessionName
import com.gachon.mowa.util.ApplicationClass.Companion.sessionsClient
import com.gachon.mowa.util.ApplicationClass.Companion.showToast
import com.gachon.mowa.util.ApplicationClass.Companion.startHour
import com.gachon.mowa.util.ApplicationClass.Companion.startMinute
import com.gachon.mowa.util.ApplicationClass.Companion.uuid
import com.gachon.mowa.util.isAlarmPermission
import com.google.android.material.navigation.NavigationView
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.dialogflow.v2.SessionName
import com.google.cloud.dialogflow.v2.SessionsClient
import com.google.cloud.dialogflow.v2.SessionsSettings
import com.google.common.collect.Lists
import java.util.*

/**
 * Float the HomeFragment, SpeakerFragment, and PhoneBookFragment
 */
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate),
    NavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val TAG: String = "ACT/MAIN"

        val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE
        )

    }

    lateinit var contextMain: Context
    private lateinit var drawerSwitch: SwitchCompat
    private lateinit var homeMenuItem: MenuItem
    private lateinit var roomDatabase: AppDatabase
    private lateinit var mPopupWindow: PopupWindow

    /**
     * onCreate 이후에 추가할 작업들을 넣어준다.
     */
    override fun initAfterBinding() {
        homeMenuItem = binding.mainBnvL.mainBnv.menu.getItem(1)
        binding.mainBnvL.mainBnvCenterIv.bringToFront()

        // 권한 허용
        ActivityCompat.requestPermissions(
            this,
            PERMISSIONS,
            PERMISSIONS_REQUEST_READ_LOCATION
        )

        initBottomNavigationView()
        initDrawerLayout()
        initClickListener()

        initDialogflow()
    }

    override fun onResume() {
        super.onResume()
        contextMain = this

        /* 알림 권한 허용 여부에 따른 스위치 초기 상태를 지정한다. */

        if (isAlarmPermission(this)) {
            // 알림 권한이 허용되어 있는 경우
            drawerSwitch.toggle()
            drawerSwitch.isChecked = true
        } else {
            // 알림 권한이 허용되어 있지 않은 경우
            drawerSwitch.isChecked = false
        }
    }

    /**
     * BottomNavigationView 초기화
     * 각 아이콘을 클릭하면 해당하는 fragment를 띄워 보여준다.
     */
    private fun initBottomNavigationView() {
        // 디폴트로는 홈 화면을 보여준다.
        // 여기서 isChecked = ture를 해야만 홈이 아닌 나머지 두 개의 아이콘(speaker, phone book)에 표시가 되지 않는다.
        homeMenuItem.isChecked = true
        replaceFragment(HomeFragment())

        // 클릭 리스너를 정의한다.
        binding.mainBnvL.mainBnv.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.bnv_speaker_item -> {
                    // speaker 아이콘을 클릭했을 때 SpeakerFragment를 띄운다.
                    replaceFragment(SpeakerFragment())
                    return@setOnItemSelectedListener true
                }

                R.id.bnv_telephone_book_item -> {
                    // phone book 아이콘을 클릭했을 때 PhoneBookFragment를 띄운다.
                    replaceFragment(PhoneBookFragment())
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

    /**
     * DrawerLayout(설정 메뉴 레이아웃) 초기화
     */
    private fun initDrawerLayout() {
        binding.mainNv.setNavigationItemSelectedListener(this)
        val menuItem = binding.mainNv.menu.findItem(R.id.setting_alarm_item)

        // 이때 actionView 부분에서 오류가 발생할 때 레이아웃 XML 코드에서 "app":actionLayout으로 알맞게 되어 있는지 확인할 것
        drawerSwitch =
            menuItem.actionView.findViewById(R.id.setting_menu_switch) as SwitchCompat
    }

    /**
     * 설정 메뉴 레이아웃의 메뉴에 대한 이벤트 처리
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            // 알림 권한
            R.id.setting_alarm_item -> {
                // 알림 권한을 설정할 수 있는 인텐트를 띄운다.
                // 토글을 이용해 알림을 끄고 켜는 게 아닌 메뉴를 클릭해 인텐트를 실행시키는 방법으로 변경
                startActivity(getNotificationIntent(packageName, applicationInfo?.uid!!))
            }

            // 방해 금지 시간 설정
            R.id.setting_alarm_time_item -> {
                // 팝업창으로 방해 금지 시간을 설정할 수 있도록 한다.
                // 방해 금지 시간으로 설정된 시간 동안엔 알림이 울리지 않는다.
                showTimePickerPopupWindow()
            }

            // 계정/정보 관리
            R.id.setting_account_item -> {
                // 계정 정보 화면을 띄운다.
                // 프로필, 이름, 이메일 등을 확인할 수 있다.
                startNextActivity(AccountActivity::class.java)
            }

            // 센서/기기 관리
            R.id.setting_sensor_item -> {
                // 센서/기기 관리 화면을 띄운다.
                startNextActivity(SensorActivity::class.java)
            }

            // 로그아웃
            R.id.setting_logout_item -> {
                // 구글 계정 로그아웃
                showLogoutDialog()
            }

            // 앱 소개
            R.id.setting_app_intro_item -> {
                // 시작할 때 보여주는 앱 소개 화면 3개를 띄운다.
                startNextActivity(IntroductionActivity::class.java)
            }

            // 사용 방법 도움말
            R.id.setting_guide_item -> {
                // 앱 사용 방법 가이드라인을 띄운다.
                startNextActivity(GuideActivity::class.java)
            }

            // 개인정보 처리방침
            R.id.setting_privacy_item -> {
                // 개인정보 처리방침에 대한 내용을 안내한다.
                startNextActivity(PolicyActivity::class.java)
            }
        }

        return false
    }

    /**
     * 클릭 이벤트/리스너 초기화
     */
    private fun initClickListener() {
        // 하단 중앙 버튼 (홈 버튼) 클릭했을 때
        binding.mainBnvL.mainBnvCenterIv.setOnClickListener {
            homeMenuItem.isChecked = true
            replaceFragment(HomeFragment())
        }

        // 설정 메뉴 아이콘을 클릭했을 때
        binding.mainBnvL.mainBnvSettingIv.setOnClickListener {
            if (!binding.mainDl.isDrawerOpen(GravityCompat.START)) {
                // 설정 메뉴창이 닫혀있을 경우 열어줍니다.
                binding.mainDl.openDrawer(GravityCompat.START)
            }
        }

        // 설정 메뉴창에 있는 메뉴 아이콘을 클릭했을 때
        val headerView = binding.mainNv.getHeaderView(0)
        headerView.findViewById<ImageView>(R.id.main_header_iv).setOnClickListener {
            // 설정 메뉴창을 닫습니다.
            binding.mainDl.closeDrawer(GravityCompat.START)
        }
    }

    /**
     * 방해 금지 시간을 설정하는 팝업창을 띄운다.
     */
    private fun showTimePickerPopupWindow() {
        val size = this.windowManager?.currentWindowMetricsPointCompat()
        val width = ((size?.x ?: 0) * 0.8f).toInt()
        val height = ((size?.y ?: 0) * 0.6f).toInt()

        // 팝업 윈도우를 띄운다.
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popupwindow_timepicker, null)
        mPopupWindow = PopupWindow(popupView, width, WindowManager.LayoutParams.WRAP_CONTENT)
        mPopupWindow.animationStyle = R.style.popup_animation
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true

        binding.mainBnvL.mainBgV.visibility = View.VISIBLE
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())

        /* TimePicker */

        val startTimePicker =
            mPopupWindow.contentView.findViewById<TimePicker>(R.id.timepicker_start_time_tp)
        val endTimePicker =
            mPopupWindow.contentView.findViewById<TimePicker>(R.id.timepicker_end_time_tp)
        val setBtn = mPopupWindow.contentView.findViewById<AppCompatButton>(R.id.timepicker_set_btn)
        val unsetBtn =
            mPopupWindow.contentView.findViewById<AppCompatButton>(R.id.timepicker_unset_btn)

        startTimePicker.setIs24HourView(true)
        endTimePicker.setIs24HourView(true)

        startTimePicker.hour = if (startHour != -1) startHour else 0
        startTimePicker.minute = if (startMinute != -1) startMinute else 0
        endTimePicker.hour = if (endHour != -1) endHour else 0
        endTimePicker.minute = if (endMinute != -1) endMinute else 0

        // 팝업 윈도우 보이기
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, -50)

        // 입력 완료 버튼을 눌렀을 때
        setBtn.setOnClickListener {
            // 알림이 울리지 않는 방해 금지 시간을 설정한다.
            startHour = startTimePicker.hour
            startMinute = startTimePicker.minute * TIME_PICKER_INTERVAL
            endHour = endTimePicker.hour
            endMinute = endTimePicker.minute * TIME_PICKER_INTERVAL
            mPopupWindow.dismiss()

            Log.d(TAG, "방해 금지 시간 $startHour:$startMinute~$endHour:$endMinute")
            showToast(this, "방해 금지 시간이 설정되었습니다.")
            binding.mainBnvL.mainBnvDndIv.visibility = View.VISIBLE
        }

        // 초기화 버튼을 눌렀을 때
        unsetBtn.setOnClickListener {
            // 알림이 울리지 않는 방해 금지 시간을 설정한다.
            startHour = -1
            startMinute = -1
            endHour = -1
            endMinute = -1
            mPopupWindow.dismiss()

            Log.d(TAG, "방해 금지 시간 $startHour:$startMinute~$endHour:$endMinute")
            showToast(this, "방해 금지 시간이 해제되었습니다.")
            binding.mainBnvL.mainBnvDndIv.visibility = View.INVISIBLE
        }

        binding.mainDl.closeDrawer(GravityCompat.START)
    }

    /**
     * 로그아웃 확인 팝업창을 띄운다.
     */
    private fun showLogoutDialog() {
        AlertDialog.Builder(this, R.style.dialog)
            .setTitle("로그아웃")
            .setMessage("로그아웃하시겠습니까?")
            .setIcon(R.drawable.ic_mowa_not_title)
            .setPositiveButton("예") { _, _ ->
                // 로그아웃 '예'를 클릭한 경우
                ((LoginActivity.contextLogin) as LoginActivity).googleSignOut(contextMain)
            }
            .setNegativeButton("아니오") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // 팝업창 닫을 때
    inner class PopupWindowDismissListener() : PopupWindow.OnDismissListener {
        override fun onDismiss() {
            binding.mainBnvL.mainBgV.visibility = View.INVISIBLE
        }
    }

    /**
     * Dialogflow 초기화
     */
    private fun initDialogflow() {

        try {
            val inputStream = this.resources.openRawResource(R.raw.credential)
            val credentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"))
            val projectId = (credentials as ServiceAccountCredentials).projectId

            val settingsBuilder = SessionsSettings.newBuilder()
            val sessionsSettings = settingsBuilder.setCredentialsProvider(
                FixedCredentialsProvider.create(credentials)
            ).build()

            sessionsClient = SessionsClient.create(sessionsSettings)
            sessionName = SessionName.of(projectId, uuid)
        } catch (e: Exception) {
            Log.d(ApplicationClass.TAG_APP, "initDialogflow/e: $e")
        }
    }

    /**
     * Dialogflow client access token 값을 가져온다.
     */
    private fun getDialogflowAccessToken(context: Context): AccessToken {
        val fileInputStream = context.resources.openRawResource(R.raw.credential)
        credentials = GoogleCredentials.fromStream(fileInputStream)

        if (credentials.createScopedRequired()) {
            credentials =
                credentials.createScoped(Collections.singletonList("https://www.googleapis.com/auth/dialogflow"))
        }

        // Access token이 만료되었을 때 호출한다.
        Thread{
            // android.os.NetworkOnMainThreadException 오류를 방지하기 위해 Thread {} 로 감싸준다.
            credentials.refreshIfExpired()
        }.start()

        return credentials.accessToken
    }
}
