package com.gachon.mowa.ui.main.home

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gachon.mowa.base.BaseFragment
import com.gachon.mowa.data.remote.activity.ActivityService
import com.gachon.mowa.data.remote.activity.ActivityStats
import com.gachon.mowa.data.remote.activity.ActivityStatsView
import com.gachon.mowa.data.remote.user.UserService
import com.gachon.mowa.data.remote.weather.OpenWeatherService
import com.gachon.mowa.data.remote.weather.OpenWeatherView
import com.gachon.mowa.databinding.FragmentHomeLargeBinding
import com.gachon.mowa.util.ApplicationClass
import com.gachon.mowa.util.getLatitude
import com.gachon.mowa.util.getLongitude
import com.gachon.mowa.util.getUserEmail
import com.gachon.mowa.viewmodel.StatsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class HomeLargeFragment : BaseFragment<FragmentHomeLargeBinding>(FragmentHomeLargeBinding::inflate), OpenWeatherView, ActivityStatsView {
    companion object {
        const val TAG = "FRAG/HOME-LARGE"
    }

    private var year: Int = 0
    private var month: Int = 0

    private var todayWeather: String = ""
    private var todayTemperature: Float = 0F
    private var wifiSsid: String = ""
    private var dateTime: String = ""

    // API
    private lateinit var openWeatherService: OpenWeatherService
    private lateinit var wifiManager: WifiManager
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var activityService: ActivityService
    private lateinit var userService: UserService

    // Stats
    private var statsThread = Thread()
    private val activityStatsViewModel: StatsViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun initAfterBinding() {
        // 날짜 설정
        dateTime = getDateTime()
        year = Integer.parseInt(getDateTime().substring(0, 4))
        month = Integer.parseInt(getDateTime().substring(5, 7))

        // 현재 날짜에 알맞는 글자를 화면에 띄운다.
        binding.homeTitleTv.text = "${month}월의 기록"

//        initService()
//        initLocation()
//        initWiFi()
//        initClickListener()
        statsThread.start()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // LiveData
        activityStatsViewModel.stats.observe(viewLifecycleOwner, Observer {
            // 변경된 내용을 뷰에 반영해줍니다.
            val activityStats = activityStatsViewModel.stats.value as ActivityStats
            binding.homeWarningCountTv.text = activityStats.warningCount.toString()
            binding.homeActivityCountTv.text = activityStats.activityCount.toString()
            binding.homeSpeakerCountTv.text = activityStats.speakerCount.toString()
            binding.homeFallCountTv.text = activityStats.fallCount.toString()
        })
    }

    override fun onResume() {
        super.onResume()

        initService()
        initLocation()
        initWiFi()
        initClickListener()
    }

    override fun onPause() {
        super.onPause()
        statsThread.interrupt()
    }

    /**
     * 클릭 이벤트 초기화
     */
    @SuppressLint("SetTextI18n")
    private fun initClickListener() {

        // 이전 화살표 아이콘을 클릭했을 때
        binding.homePreviousMonthIv.setOnClickListener {
            // 이전 달 통계를 보여준다.
            year = if (month > 1) year else year - 1
            month = if (month == 1) 12 else month - 1
            binding.homeTitleTv.text = "${month}월의 기록"

            // 이전 달 통계에 대한 API를 호출한다.
            activityService.getMonthlyActivityStatsByUserId(
                this,
                getUserEmail()!!,
                year,
                month
            )
        }

        // 다음 화살표 아이콘을 클릭했을 때
        binding.homeNextMonthIv.setOnClickListener {
            // 다음 달 통계를 보여준다.
            year = if (month < 12) year else year + 1
            month = if (month == 12) 1 else (month + 1)
            binding.homeTitleTv.text = "${month}월의 기록"

            // 다음 달 통계에 대한 API를 호출한다.
            activityService.getMonthlyActivityStatsByUserId(
                this,
                getUserEmail()!!,
                year,
                month
            )
        }
    }

    /**
     * 서비스 클래스 초기화
     */
    private fun initService() {
        openWeatherService = OpenWeatherService()
        activityService = ActivityService()
        userService = UserService()

        // Wi-Fi
        wifiManager =
            requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        connectivityManager =
            requireContext().applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // 활동 통계를 받아오는 API 호출 스레드
        statsThread = Thread {
            kotlin.run {
                try {
                    while (!statsThread.isInterrupted) {
                        // delay
                        Thread.sleep(5000)

                        // 만약 UI에 접근하려면 runOnUiThread { }를 이용해야 한다.

                        // API 호출
                        activityService.getMonthlyActivityStatsByUserId(
                            this,
                            getUserEmail()!!,
                            year,
                            month
                        )
                    }

                } catch (e: InterruptedException) {
                    statsThread.interrupt()
                }
            }
        }
    }

    /**
     * 사용자의 위치 정보 초기화
     */
    private fun initLocation() {
        // Main 디스패쳐로 UI 제외
        CoroutineScope(Dispatchers.Main).launch {
            /* 위치 정보 -> 날씨 정보 API -> 뷰에 반영 */

            // 날씨 정보 API
            initOpenWeatherService()
        }
    }

    /**
     * 날씨 API
     * 스마트폰 GPS 위치에 따라 자동으로 해당 지역 날씨 및 온도를 제공합니다.
     */
    private suspend fun initOpenWeatherService() {
        withContext(Dispatchers.IO) {
            openWeatherService.getOpenWeather(this@HomeLargeFragment, getLatitude(), getLongitude())
        }
    }

    /**
     * 날씨 API 성공했을 때
     */
    override fun onGetWeatherSuccess(todayWeather: String, todayTemperature: Float) {
        this.todayWeather = todayWeather
        this.todayTemperature = todayTemperature

        setTodayWeather()
    }

    /**
     * 날씨 API 실패했을 때
     */
    override fun onGetWeatherFailure(code: Int, message: String) {
        Log.d(TAG, "onGetWeatherFailure/code: $code, message: $message")
        ApplicationClass.showToast(requireContext(), "날씨 정보를 불러오지 못했습니다.")
    }

    /**
     * 뷰에 날씨 API로부터 받아온 정보를 띄워줍니다.
     */
    private fun setTodayWeather() {
        when (todayWeather) {
            "Clear" -> {
                binding.homeWeatherIconTv.text = "☀"
                binding.homeWeatherTv.text = "오늘의 날씨는 맑음"
            }
            "Clouds" -> {
                binding.homeWeatherIconTv.text = "☁"
                binding.homeWeatherTv.text = "오늘의 날씨는 구름"
            }
            "Rain" -> {
                binding.homeWeatherIconTv.text = "🌧"
                binding.homeWeatherTv.text = "오늘의 날씨는 비"
            }
            "Thunderstorm" -> {
                binding.homeWeatherIconTv.text = "🌩"
                binding.homeWeatherTv.text = "오늘의 날씨는 뇌우"
            }
            "Snow" -> {
                binding.homeWeatherIconTv.text = "🌨"
                binding.homeWeatherTv.text = "오늘의 날씨는 눈"
            }
            else -> {
                binding.homeWeatherIconTv.text = "🌫"
                binding.homeWeatherTv.text = "오늘의 날씨는 안개"
            }
        }

        val weatherDescription =
            "오늘의 기온은 " + String.format(Locale.getDefault(), "%.1f", todayTemperature) + "℃"
        binding.homeTemperatureTv.text = weatherDescription

        Log.d(TAG, "setTodayWeather/todayWeather: $todayWeather")
        Log.d(TAG, "setTodayWeather/weatherDescription: $weatherDescription")
    }

    /**
     * Wi-Fi 정보를 초기화합니다.
     */
    private fun initWiFi() {
        CoroutineScope(Dispatchers.Main).launch {
            initWifiManager()

            // Wi-Fi SSID 값을 가져오지 못했을 때에 맞는 처리를 해준다.
            Log.d(TAG, "initWiFi/wifiSsid: $wifiSsid")
            if (wifiSsid == "<unknown ssid>") {
                initWifiManager()
            }
            else {
                binding.homeWifiTv.text = wifiSsid
            }
        }
    }

    /**
     * Wi-Fi 정보를 가져옵니다.
     */
    private suspend fun initWifiManager() {
        withContext(Dispatchers.IO) {
            val wifiInfo = wifiManager.connectionInfo
            wifiSsid = wifiInfo.ssid.toString()
        }
    }

    /**
     * 현재 날짜 구하기
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDateTime(): String {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return now.format(formatter)
    }

    /**
     * 활동 통계 데이터 불러오기를 성공했을 때
     */
    override fun onActivityStatsSuccess(activityStats: ActivityStats) {
        Log.d(TAG, "activityStats: $activityStats")

        // ViewModel에 데이터 객체를 넣어줍니다.
        // FIXME: 만약 업데이트 & LiveData 뷰 반영이 안 될 경우 이 부분을 수정해야 한다.
        activityStatsViewModel.setStats(activityStats)
    }

    /**
     * 활동 통계 데이터 불러오기를 실패했을 때
     */
    override fun onActivityStatsFailure(message: String) {
//        showToast(, "활동 통계를 불러오지 못했습니다.")
    }

}