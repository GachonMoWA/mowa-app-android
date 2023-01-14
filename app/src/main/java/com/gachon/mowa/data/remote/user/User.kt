package com.gachon.mowa.data.remote.user

import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class User (
    @SerializedName("user_id") val userId: String,
    @SerializedName("mac_address") var macAddress: String?,
    @SerializedName("serial_number") var serialNumber: JSONObject?
//    @SerializedName("user_name") var userName: String
)
