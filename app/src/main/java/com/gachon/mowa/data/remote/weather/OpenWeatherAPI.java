package com.gachon.mowa.data.remote.weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface OpenWeatherAPI {

    @GET("weather")
    Call<OpenWeather> getWeather(@Query("lat") double lat,
                                 @Query("lon") double lot,
                                 @Query("appid") String appKey,
                                 @Query("units") String unit
    );
}
