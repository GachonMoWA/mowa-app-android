package com.gachon.mowa.data.remote.weather

interface OpenWeatherView {
    fun onGetWeatherSuccess(todayWeather: String, todayTemperature: Float)
    fun onGetWeatherFailure(code: Int, message: String)
}
