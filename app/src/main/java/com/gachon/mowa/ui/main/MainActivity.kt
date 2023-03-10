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
     * onCreate ????????? ????????? ???????????? ????????????.
     */
    override fun initAfterBinding() {
        homeMenuItem = binding.mainBnvL.mainBnv.menu.getItem(1)
        binding.mainBnvL.mainBnvCenterIv.bringToFront()

        // ?????? ??????
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

        /* ?????? ?????? ?????? ????????? ?????? ????????? ?????? ????????? ????????????. */

        if (isAlarmPermission(this)) {
            // ?????? ????????? ???????????? ?????? ??????
            drawerSwitch.toggle()
            drawerSwitch.isChecked = true
        } else {
            // ?????? ????????? ???????????? ?????? ?????? ??????
            drawerSwitch.isChecked = false
        }
    }

    /**
     * BottomNavigationView ?????????
     * ??? ???????????? ???????????? ???????????? fragment??? ?????? ????????????.
     */
    private fun initBottomNavigationView() {
        // ??????????????? ??? ????????? ????????????.
        // ????????? isChecked = ture??? ????????? ?????? ?????? ????????? ??? ?????? ?????????(speaker, phone book)??? ????????? ?????? ?????????.
        homeMenuItem.isChecked = true
        replaceFragment(HomeFragment())

        // ?????? ???????????? ????????????.
        binding.mainBnvL.mainBnv.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.bnv_speaker_item -> {
                    // speaker ???????????? ???????????? ??? SpeakerFragment??? ?????????.
                    replaceFragment(SpeakerFragment())
                    return@setOnItemSelectedListener true
                }

                R.id.bnv_telephone_book_item -> {
                    // phone book ???????????? ???????????? ??? PhoneBookFragment??? ?????????.
                    replaceFragment(PhoneBookFragment())
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

    /**
     * DrawerLayout(?????? ?????? ????????????) ?????????
     */
    private fun initDrawerLayout() {
        binding.mainNv.setNavigationItemSelectedListener(this)
        val menuItem = binding.mainNv.menu.findItem(R.id.setting_alarm_item)

        // ?????? actionView ???????????? ????????? ????????? ??? ???????????? XML ???????????? "app":actionLayout?????? ????????? ?????? ????????? ????????? ???
        drawerSwitch =
            menuItem.actionView.findViewById(R.id.setting_menu_switch) as SwitchCompat
    }

    /**
     * ?????? ?????? ??????????????? ????????? ?????? ????????? ??????
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            // ?????? ??????
            R.id.setting_alarm_item -> {
                // ?????? ????????? ????????? ??? ?????? ???????????? ?????????.
                // ????????? ????????? ????????? ?????? ?????? ??? ?????? ????????? ????????? ???????????? ??????????????? ???????????? ??????
                startActivity(getNotificationIntent(packageName, applicationInfo?.uid!!))
            }

            // ?????? ?????? ?????? ??????
            R.id.setting_alarm_time_item -> {
                // ??????????????? ?????? ?????? ????????? ????????? ??? ????????? ??????.
                // ?????? ?????? ???????????? ????????? ?????? ????????? ????????? ????????? ?????????.
                showTimePickerPopupWindow()
            }

            // ??????/?????? ??????
            R.id.setting_account_item -> {
                // ?????? ?????? ????????? ?????????.
                // ?????????, ??????, ????????? ?????? ????????? ??? ??????.
                startNextActivity(AccountActivity::class.java)
            }

            // ??????/?????? ??????
            R.id.setting_sensor_item -> {
                // ??????/?????? ?????? ????????? ?????????.
                startNextActivity(SensorActivity::class.java)
            }

            // ????????????
            R.id.setting_logout_item -> {
                // ?????? ?????? ????????????
                showLogoutDialog()
            }

            // ??? ??????
            R.id.setting_app_intro_item -> {
                // ????????? ??? ???????????? ??? ?????? ?????? 3?????? ?????????.
                startNextActivity(IntroductionActivity::class.java)
            }

            // ?????? ?????? ?????????
            R.id.setting_guide_item -> {
                // ??? ?????? ?????? ?????????????????? ?????????.
                startNextActivity(GuideActivity::class.java)
            }

            // ???????????? ????????????
            R.id.setting_privacy_item -> {
                // ???????????? ??????????????? ?????? ????????? ????????????.
                startNextActivity(PolicyActivity::class.java)
            }
        }

        return false
    }

    /**
     * ?????? ?????????/????????? ?????????
     */
    private fun initClickListener() {
        // ?????? ?????? ?????? (??? ??????) ???????????? ???
        binding.mainBnvL.mainBnvCenterIv.setOnClickListener {
            homeMenuItem.isChecked = true
            replaceFragment(HomeFragment())
        }

        // ?????? ?????? ???????????? ???????????? ???
        binding.mainBnvL.mainBnvSettingIv.setOnClickListener {
            if (!binding.mainDl.isDrawerOpen(GravityCompat.START)) {
                // ?????? ???????????? ???????????? ?????? ???????????????.
                binding.mainDl.openDrawer(GravityCompat.START)
            }
        }

        // ?????? ???????????? ?????? ?????? ???????????? ???????????? ???
        val headerView = binding.mainNv.getHeaderView(0)
        headerView.findViewById<ImageView>(R.id.main_header_iv).setOnClickListener {
            // ?????? ???????????? ????????????.
            binding.mainDl.closeDrawer(GravityCompat.START)
        }
    }

    /**
     * ?????? ?????? ????????? ???????????? ???????????? ?????????.
     */
    private fun showTimePickerPopupWindow() {
        val size = this.windowManager?.currentWindowMetricsPointCompat()
        val width = ((size?.x ?: 0) * 0.8f).toInt()
        val height = ((size?.y ?: 0) * 0.6f).toInt()

        // ?????? ???????????? ?????????.
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

        // ?????? ????????? ?????????
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, -50)

        // ?????? ?????? ????????? ????????? ???
        setBtn.setOnClickListener {
            // ????????? ????????? ?????? ?????? ?????? ????????? ????????????.
            startHour = startTimePicker.hour
            startMinute = startTimePicker.minute * TIME_PICKER_INTERVAL
            endHour = endTimePicker.hour
            endMinute = endTimePicker.minute * TIME_PICKER_INTERVAL
            mPopupWindow.dismiss()

            Log.d(TAG, "?????? ?????? ?????? $startHour:$startMinute~$endHour:$endMinute")
            showToast(this, "?????? ?????? ????????? ?????????????????????.")
            binding.mainBnvL.mainBnvDndIv.visibility = View.VISIBLE
        }

        // ????????? ????????? ????????? ???
        unsetBtn.setOnClickListener {
            // ????????? ????????? ?????? ?????? ?????? ????????? ????????????.
            startHour = -1
            startMinute = -1
            endHour = -1
            endMinute = -1
            mPopupWindow.dismiss()

            Log.d(TAG, "?????? ?????? ?????? $startHour:$startMinute~$endHour:$endMinute")
            showToast(this, "?????? ?????? ????????? ?????????????????????.")
            binding.mainBnvL.mainBnvDndIv.visibility = View.INVISIBLE
        }

        binding.mainDl.closeDrawer(GravityCompat.START)
    }

    /**
     * ???????????? ?????? ???????????? ?????????.
     */
    private fun showLogoutDialog() {
        AlertDialog.Builder(this, R.style.dialog)
            .setTitle("????????????")
            .setMessage("???????????????????????????????")
            .setIcon(R.drawable.ic_mowa_not_title)
            .setPositiveButton("???") { _, _ ->
                // ???????????? '???'??? ????????? ??????
                ((LoginActivity.contextLogin) as LoginActivity).googleSignOut(contextMain)
            }
            .setNegativeButton("?????????") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // ????????? ?????? ???
    inner class PopupWindowDismissListener() : PopupWindow.OnDismissListener {
        override fun onDismiss() {
            binding.mainBnvL.mainBgV.visibility = View.INVISIBLE
        }
    }

    /**
     * Dialogflow ?????????
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
     * Dialogflow client access token ?????? ????????????.
     */
    private fun getDialogflowAccessToken(context: Context): AccessToken {
        val fileInputStream = context.resources.openRawResource(R.raw.credential)
        credentials = GoogleCredentials.fromStream(fileInputStream)

        if (credentials.createScopedRequired()) {
            credentials =
                credentials.createScoped(Collections.singletonList("https://www.googleapis.com/auth/dialogflow"))
        }

        // Access token??? ??????????????? ??? ????????????.
        Thread{
            // android.os.NetworkOnMainThreadException ????????? ???????????? ?????? Thread {} ??? ????????????.
            credentials.refreshIfExpired()
        }.start()

        return credentials.accessToken
    }
}
