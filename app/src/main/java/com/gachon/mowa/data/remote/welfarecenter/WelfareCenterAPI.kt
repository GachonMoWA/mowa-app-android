package com.gachon.mowa.data.remote.welfarecenter

import com.google.api.client.json.Json
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WelfareCenterAPI {

    // 만약 따로 GET url이 없다면 "."으로 지정해 주면 된다.
    @GET(".")
    fun getWelfareCenters(
        @Query("KEY") key: String,
        @Query("Type") type: String,
        @Query("pIndex") pageIndex: Int,
        @Query("pSize") pageSize: Int,
        @Query("SIGUNGU_NM") region: String?,
        @Query("FACLT_NM") faculty: String?
    ): Call<JsonObject>

}
