package com.gachon.mowa.ui.main.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Context.WIFI_SERVICE
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gachon.mowa.base.BaseFragment
import com.gachon.mowa.data.remote.activity.ActivityService
import com.gachon.mowa.data.remote.activity.ActivityStats
import com.gachon.mowa.data.remote.activity.ActivityStatsView
import com.gachon.mowa.data.remote.user.UserService
import com.gachon.mowa.data.remote.weather.OpenWeatherService
import com.gachon.mowa.data.remote.weather.OpenWeatherView
import com.gachon.mowa.databinding.FragmentHomeBinding
import com.gachon.mowa.util.ApplicationClass.Companion.showToast
import com.gachon.mowa.util.getUserEmail
import com.gachon.mowa.viewmodel.StatsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate), OpenWeatherView, ActivityStatsView {
    companion object {
        const val TAG = "FRAG/HOME"
    }

    private var year: Int = 0
    private var month: Int = 0

    private var todayWeather: String = ""
    private var todayTemperature: Float = 0F
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var wifiSsid: String = ""
    private var dateTime: String = ""

//    private lateinit var binding: FragmentHomeBinding
    private lateinit var locationManager: LocationManager
    private lateinit var gpsListener: GPSListener

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
        // ?????? ??????
        dateTime = getDateTime()
        year = Integer.parseInt(getDateTime().substring(0, 4))
        month = Integer.parseInt(getDateTime().substring(5, 7))

        // ?????? ????????? ????????? ????????? ????????? ?????????.
        binding.homeTitleTv.text = "${month}?????? ??????"

        initService()
        initLocation()
        initWiFi()
        initClickListener()
        statsThread.start()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // LiveData
        activityStatsViewModel.stats.observe(viewLifecycleOwner, Observer {
            // ????????? ????????? ?????? ??????????????????.
            val activityStats = activityStatsViewModel.stats.value as ActivityStats
            binding.homeWarningCountTv.text = activityStats.warningCount.toString()
            binding.homeActivityCountTv.text = activityStats.activityCount.toString()
            binding.homeSpeakerCountTv.text = activityStats.speakerCount.toString()
            binding.homeFallCountTv.text = activityStats.fallCount.toString()
        })
    }

    override fun onPause() {
        super.onPause()
        statsThread.interrupt()
    }

    /**
     * ?????? ????????? ?????????
     */
    @SuppressLint("SetTextI18n")
    private fun initClickListener() {

        // ?????? ????????? ???????????? ???????????? ???
        binding.homePreviousMonthIv.setOnClickListener {
            // ?????? ??? ????????? ????????????.
            year = if (month > 1) year else year - 1
            month = if (month == 1) 12 else month - 1
            binding.homeTitleTv.text = "${month}?????? ??????"

            // ?????? ??? ????????? ?????? API??? ????????????.
            activityService.getMonthlyActivityStatsByUserId(
                this,
                getUserEmail()!!,
                year,
                month
            )
        }

        // ?????? ????????? ???????????? ???????????? ???
        binding.homeNextMonthIv.setOnClickListener {
            // ?????? ??? ????????? ????????????.
            year = if (month < 12) year else year + 1
            month = if (month == 12) 1 else (month + 1)
            binding.homeTitleTv.text = "${month}?????? ??????"

            // ?????? ??? ????????? ?????? API??? ????????????.
            activityService.getMonthlyActivityStatsByUserId(
                this,
                getUserEmail()!!,
                year,
                month
            )
        }
    }

    /**
     * ????????? ????????? ?????????
     */
    private fun initService() {
        openWeatherService = OpenWeatherService()
        activityService = ActivityService()
        userService = UserService()

        // Wi-Fi
        wifiManager =
            requireContext().applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        connectivityManager =
            requireContext().applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // ?????? ????????? ???????????? API ?????? ?????????
        statsThread = Thread {
            kotlin.run {
                try {
                    while (!statsThread.isInterrupted) {
                        // delay
                        Thread.sleep(5000)

                        // ?????? UI??? ??????????????? runOnUiThread { }??? ???????????? ??????.

                        // API ??????
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
     * ???????????? ?????? ?????? ?????????
     */
    private fun initLocation() {
        // Main ??????????????? UI ??????
        CoroutineScope(Dispatchers.Main).launch {
            /* ?????? ?????? -> ?????? ?????? API -> ?????? ?????? */

            // ?????? ??????
            startLocationService()

            // ?????? ?????? API
            initOpenWeatherService()
        }
    }

    /**
     * ???????????? ?????? ????????? ???????????? ?????????
     */
    private fun startLocationService() {
        locationManager = requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager

        try {
            gpsListener = GPSListener()

            // Main thread?????? ??????????????? ??????.
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0,
                0f,
                gpsListener
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    /**
     * ????????? ?????? ??????(??????, ??????)??? ????????????.
     * ????????? ???????????? ?????? ???????????? ?????? API??? ????????????.
     */
    inner class GPSListener : LocationListener {
        override fun onLocationChanged(p0: Location) {
            latitude = p0.latitude
            longitude = p0.longitude

            locationManager.removeUpdates(gpsListener)
        }
        //????????? ?????? ??????
        override fun onProviderDisabled(provider: String) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    }

    /**
     * ?????? API
     * ???????????? GPS ????????? ?????? ???????????? ?????? ?????? ?????? ??? ????????? ???????????????.
     */
    private suspend fun initOpenWeatherService() {
        withContext(Dispatchers.IO) {
            openWeatherService.getOpenWeather(this@HomeFragment, latitude, longitude)
        }
    }

    /**
     * ?????? API ???????????? ???
     */
    override fun onGetWeatherSuccess(todayWeather: String, todayTemperature: Float) {
        this.todayWeather = todayWeather
        this.todayTemperature = todayTemperature

        setTodayWeather()
    }

    /**
     * ?????? API ???????????? ???
     */
    override fun onGetWeatherFailure(code: Int, message: String) {
        Log.d(TAG, "onGetWeatherFailure/code: $code, message: $message")
        showToast(requireContext(), "?????? ????????? ???????????? ???????????????.")
    }

    /**
     * ?????? ?????? API????????? ????????? ????????? ???????????????.
     */
    private fun setTodayWeather() {
        when (todayWeather) {
            "Clear" -> {
                binding.homeWeatherIconTv.text = "???"
                binding.homeWeatherTv.text = "????????? ????????? ??????"
            }
            "Clouds" -> {
                binding.homeWeatherIconTv.text = "???"
                binding.homeWeatherTv.text = "????????? ????????? ??????"
            }
            "Rain" -> {
                binding.homeWeatherIconTv.text = "????"
                binding.homeWeatherTv.text = "????????? ????????? ???"
            }
            "Thunderstorm" -> {
                binding.homeWeatherIconTv.text = "????"
                binding.homeWeatherTv.text = "????????? ????????? ??????"
            }
            "Snow" -> {
                binding.homeWeatherIconTv.text = "????"
                binding.homeWeatherTv.text = "????????? ????????? ???"
            }
            else -> {
                binding.homeWeatherIconTv.text = "????"
                binding.homeWeatherTv.text = "????????? ????????? ??????"
            }
        }

        val weatherDescription =
            "????????? ????????? " + String.format(Locale.getDefault(), "%.1f", todayTemperature) + "???"
        binding.homeTemperatureTv.text = weatherDescription

        Log.d(TAG, "setTodayWeather/todayWeather: $todayWeather")
        Log.d(TAG, "setTodayWeather/weatherDescription: $weatherDescription")
    }

    /**
     * Wi-Fi ????????? ??????????????????.
     */
    private fun initWiFi() {
        CoroutineScope(Dispatchers.Main).launch {
            initWifiManager()
            binding.homeWifiTv.text = wifiSsid
        }
    }

    /**
     * Wi-Fi ????????? ???????????????.
     */
    private suspend fun initWifiManager() {
        withContext(Dispatchers.IO) {
            val wifiInfo = wifiManager.connectionInfo
            wifiSsid = wifiInfo.ssid.toString()
        }
    }

    /**
     * ?????? ?????? ?????????
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDateTime(): String {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return now.format(formatter)
    }

    /**
     * ?????? ?????? ????????? ??????????????? ???????????? ???
     */
    override fun onActivityStatsSuccess(activityStats: ActivityStats) {
        Log.d(TAG, "activityStats: $activityStats")

        // ViewModel??? ????????? ????????? ???????????????.
        // FIXME: ?????? ???????????? & LiveData ??? ????????? ??? ??? ?????? ??? ????????? ???????????? ??????.
        activityStatsViewModel.setStats(activityStats)
    }

    /**
     * ?????? ?????? ????????? ??????????????? ???????????? ???
     */
    override fun onActivityStatsFailure(message: String) {
//        showToast(, "?????? ????????? ???????????? ???????????????.")
    }
}
