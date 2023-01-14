package com.gachon.mowa.data.remote.weather

import android.util.Log
import com.gachon.mowa.BuildConfig
import kotlinx.coroutines.CoroutineScope
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OpenWeatherService {
    companion object {
        const val TAG = "SERVICE/OPEN-WEATHER"
    }

    // 파라미터 위도, 경도, API KEY, 표시 제외 항목, 단위
    fun getOpenWeather(openWeatherView: OpenWeatherView, latitude: Double, longitude: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.OPEN_WEATHER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val openWeatherService = retrofit.create(OpenWeatherAPI::class.java)

        openWeatherService
            .getWeather(latitude, longitude, BuildConfig.OPEN_WEATHER_API_KEY, "metric")
            .enqueue(object : Callback<OpenWeather> {

                override fun onResponse(call: Call<OpenWeather>, response: Response<OpenWeather>) {
                    val mData = response.body()!!
                    val weather = mData.weather[0]
                    val todayWeather = weather.main
                    val todayTemperature = mData.main.temp

                    openWeatherView.onGetWeatherSuccess(todayWeather, todayTemperature)
                }

                override fun onFailure(call: Call<OpenWeather>, t: Throwable) {
                    Log.d(TAG, "onFailure() because of ${t.message}")
                    openWeatherView.onGetWeatherFailure(400, "네트워크 오류")
                }
            })
    }
}
