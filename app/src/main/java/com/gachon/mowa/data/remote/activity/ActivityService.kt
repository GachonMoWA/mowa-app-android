package com.gachon.mowa.data.remote.activity

import android.util.Log
import com.gachon.mowa.data.remote.user.UserView
import com.gachon.mowa.util.ApplicationClass.Companion.retrofit
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: 전체적으로 Success 작업 수정해줘야 합니다.
class ActivityService {
    companion object {
        const val TAG: String = "SERVICE/ACTIVITY"
    }

    private val activityService: ActivityRetrofitInterface =
        retrofit.create(ActivityRetrofitInterface::class.java)

    /**
     * 모든 활동 기록을 가져옵니다.
     * @see ActivityRetrofitInterface.getAll
     */
    fun getAll(activityView: ActivityView) {

        activityService.getAll().enqueue(object : Callback<List<Activity>> {

            override fun onResponse(
                call: Call<List<Activity>>,
                response: Response<List<Activity>>
            ) {
                if (!response.isSuccessful) return

                // API 응답 성공한 경우
                when (response.code()) {
                    200 -> activityView.onActivitySuccess()
                    else -> activityView.onActivityFailure("데이터 불러오기 실패")
                }
            }

            override fun onFailure(call: Call<List<Activity>>, t: Throwable) {
                Log.d(TAG, "getAll/onFailure/${t.message}")
                activityView.onActivityFailure("네트워크 오류")
            }

        })
    }

    /**
     * 사용자 등록 후 바로 진행하도록 합니다.
     * 활동 기록을 서버에 보냅니다.
     * @see ActivityRetrofitInterface.addActivity
     */
    fun addActivity(activityView: ActivityView, activity: Activity) {
        // 인자로 전달받은 Activity 객체에는 등록한 사용자의 아이디가 저장되어 있습니다.
        // 그 외 나머지 필드들은 디폴트 값을 갖게 됩니다.

        activityService.addActivity(activity).enqueue(object : Callback<Activity> {

            override fun onResponse(call: Call<Activity>, response: Response<Activity>) {
                if (!response.isSuccessful) return

                // API 응답 성공한 경우
                when (response.code()) {
                    201 -> activityView.onActivitySuccess()
                    else -> activityView.onActivityFailure("데이터 전송 실패")
                }
            }

            override fun onFailure(call: Call<Activity>, t: Throwable) {
                Log.d(TAG, "addActivity/onFailure/${t.message}")
                activityView.onActivityFailure("네트워크 오류")
            }

        })
    }

    /**
     * 특정 사용자에 해당하는 모든 활동 기록을 가져옵니다.
     * FIXME: JsonArray가 아닌 List 형식으로 받아와도 되는지 의문
     * @see ActivityRetrofitInterface.getAllByUserId
     */
    fun getAllByUserId(activityView: ActivityView, userId: String) {

        activityService.getAllByUserId(userId).enqueue(object : Callback<List<Activity>> {

            override fun onResponse(
                call: Call<List<Activity>>,
                response: Response<List<Activity>>
            ) {
                if (!response.isSuccessful) return

                // API 응답 성공한 경우
                when (response.code()) {
                    200 -> activityView.onActivitySuccess()
                    else -> activityView.onActivityFailure("데이터 불러오기 실패")
                }
            }

            override fun onFailure(call: Call<List<Activity>>, t: Throwable) {
                Log.d(TAG, "getAllByUserId/onFailure/${t.message}")
                activityView.onActivityFailure("네트워크 오류")
            }

        })
    }

    /**
     * 특정 사용자에 특정 날짜(연월일 기준)에 해당되는 활동 기록을 서버에 보냅니다.
     * @see ActivityRetrofitInterface.addDailyActivityByUserId
     */
    fun addDailyActivityByUserId(
        activityView: ActivityView,
        userId: String,
        year: String,
        month: String,
        day: String
    ) {

        activityService.addDailyActivityByUserId(userId, year, month, day)
            .enqueue(object : Callback<ResponseBody> {

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (!response.isSuccessful) return

                    // API 응답 성공한 경우
                    when (response.code()) {
                        201 -> activityView.onActivitySuccess()
                        else -> activityView.onActivityFailure("데이터 전송 실패")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d(TAG, "addDailyActivityByUserId/onFailure/${t.message}")
                    activityView.onActivityFailure("네트워크 오류")
                }

            })
    }

    /**
     * 특정 사용자에 특정 날짜(연월 기준)에 해당하는 활동 통계를 가져옵니다.
     * @see ActivityRetrofitInterface.getMonthlyActivityStatsByUserId
     */
    fun getMonthlyActivityStatsByUserId(
        activityStatsView: ActivityStatsView,
        userId: String,
        year: Int,
        month: Int
    ) {
        activityService.getMonthlyActivityStatsByUserId(userId, year, month)
            .enqueue(object : Callback<ActivityStats> {

                override fun onResponse(
                    call: Call<ActivityStats>,
                    response: Response<ActivityStats>
                ) {
                    if (!response.isSuccessful) return

                    // API 응답 성공한 경우
                    when (response.code()) {
                        200 -> {
                            val jsonObject = JSONObject(Gson().toJson(response.body()))

                            // json parsing
                            val warningCount = jsonObject.getString("warning_count").toInt()
                            val activityCount = jsonObject.getString("activity_count").toInt()
                            val speakerCount = jsonObject.getString("speaker_count").toInt()
                            val fallCount = jsonObject.getString("fall_count").toInt()

                            val activityStats = ActivityStats(
                                userId,
                                warningCount,
                                activityCount,
                                speakerCount,
                                fallCount
                            )

                            Log.d(
                                TAG,
                                "getMonthlyActivityStatsByUserId/onResponse/activityStats: $activityStats"
                            )
                            activityStatsView.onActivityStatsSuccess(activityStats)
                        }
                        else -> {
                            activityStatsView.onActivityStatsFailure("데이터 불러오기 실패")
                        }
                    }
                }

                override fun onFailure(call: Call<ActivityStats>, t: Throwable) {
                    activityStatsView.onActivityStatsFailure("네트워크 오류")
                }
            })
    }

    /**
     * 특정 사용자에 특정 날짜(연월일 기준)에 해당되는 활동 기록을 업데이트합니다.
     * @see ActivityRetrofitInterface.updateDailyActivityByUserId
     */
    fun updateDailyActivityByUserId(
        activityView: ActivityView,
        userId: String,
        year: String,
        month: String,
        day: String
    ) {

        activityService.updateDailyActivityByUserId(userId, year, month, day)
            .enqueue(object : Callback<ResponseBody> {

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (!response.isSuccessful) return

                    // API 응답 성공한 경우
                    when (response.code()) {
                        200 -> activityView.onActivityFailure("이미 존재하는 데이터입니다.")
                        201 -> activityView.onActivitySuccess() // 신규 생성인 경우
                        else -> activityView.onActivityFailure("데이터 업데이트 실패")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d(TAG, "updateDailyActivityByUserId/onFailure/${t.message}")
                    activityView.onActivityFailure("네트워크 오류")
                }

            })
    }

    /**
     * 특정 사용자에 특정 날짜(연월일 기준)에 해당되는 활동 기록을 삭제합니다.
     */
    fun deleteDailyActivityByUserId(
        activityView: ActivityView,
        userId: String,
        year: String,
        month: String,
        day: String
    ) {

        activityService.deleteDailyActivityByUserId(userId, year, month, day)
            .enqueue(object : Callback<ResponseBody> {

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (!response.isSuccessful) return

                    // API 응답 성공한 경우
                    when (response.code()) {
                        204 -> activityView.onActivitySuccess()
                        else -> activityView.onActivityFailure("데이터 삭제 실패")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d(TAG, "deleteDailyActivityByUserId/onFailure/${t.message}")
                    activityView.onActivityFailure("네트워크 오류")
                }

            })
    }
}
