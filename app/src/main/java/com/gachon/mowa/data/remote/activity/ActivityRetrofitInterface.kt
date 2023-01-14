package com.gachon.mowa.data.remote.activity

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ActivityRetrofitInterface {

    /**
     * 모든 활동 기록을 가져옵니다.
     * @see ActivityService.getAll
     */
    @GET("/activity/")
    fun getAll(
    ): Call<List<Activity>>

    /**
     * 활동 기록을 서버에 보냅니다.
     * @see ActivityService.addActivity
     */
    @POST("/activity/")
    fun addActivity(
        @Body activity: Activity
    ): Call<Activity>

    /**
     * 특정 사용자에 해당하는 모든 활동 기록을 가져옵니다.
     * @see ActivityService.getAllByUserId
     */
    @GET("/activity/{user_id}/")
    fun getAllByUserId(
        @Path("user_id") userId: String
    ): Call<List<Activity>>

    /**
     * 특정 사용자에 특정 날짜(연월일 기준)에 해당되는 활동 기록을 서버에 보냅니다.
     * @see ActivityService.addDailyActivityByUserId
     */
    @POST("/activity/check/{user_id}/{year}/{month}/{day}/")
    fun addDailyActivityByUserId(
        @Path("user_id") userId: String,
        @Path("year") year: String,
        @Path("month") month: String,
        @Path("day") day: String
    ): Call<ResponseBody>

    /**
     * 특정 사용자에 특정 날짜(연월 기준)에 해당하는 활동 통계를 가져옵니다.
     * TODO: 실시간 업데이트는 해당 인터페이스를 사용해야 합니다.
     * @see ActivityService.getMonthlyActivityStatsByUserId
     */
    @GET("/activity/{user_id}/stats/{year}/{month}/")
    fun getMonthlyActivityStatsByUserId(
        @Path("user_id") userId: String,
        @Path("year") year: Int,
        @Path("month") month: Int
    ): Call<ActivityStats>

    /**
     * 특정 사용자에 특정 날짜(연월일 기준)에 해당되는 활동 기록을 업데이트합니다.
     * 안드로이드에서 스피커(대화하기) 이용 횟수를 업데이트하기 위해 존재 (서버로 보내야 하기 때문)
     * @see ActivityService.updateDailyActivityByUserId
     */
    @PUT("/activity/{user_id}/{year}/{month}/{day}/")
    fun updateDailyActivityByUserId(
        @Path("user_id") userId: String,
        @Path("year") year: String,
        @Path("month") month: String,
        @Path("day") day: String
    ): Call<ResponseBody>

    /**
     * 특정 사용자에 특정 날짜(연월일 기준)에 해당되는 활동 기록을 삭제합니다.
     * @see ActivityService.deleteDailyActivityByUserId
     */
    @DELETE("/activity/{user_id}/{year}/{month}/{day}/")
    fun deleteDailyActivityByUserId(
        @Path("user_id") userId: String,
        @Path("year") year: String,
        @Path("month") month: String,
        @Path("day") day: String
    ): Call<ResponseBody>
}
