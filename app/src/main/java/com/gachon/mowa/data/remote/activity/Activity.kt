package com.gachon.mowa.data.remote.activity

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class Activity(
    @SerializedName("user_id") val userID: String,
    @SerializedName("date") var date: LocalDateTime = LocalDateTime.now(),
    @SerializedName("warning_count") var warningCount: Int = 0,
    @SerializedName("activity_count") var activityCount: Int = 0,
    @SerializedName("speaker_count") var speakerCount: Int = 0,
    @SerializedName("fall_count") var fallCount: Int = 0
)

data class ActivityStats(
    @SerializedName("user_id") val userID: String,
    @SerializedName("warning_count") var warningCount: Int = 0,
    @SerializedName("activity_count") var activityCount: Int = 0,
    @SerializedName("speaker_count") var speakerCount: Int = 0,
    @SerializedName("fall_count") var fallCount: Int = 0
)
