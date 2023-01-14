package com.gachon.mowa.data.remote.welfarecenter

import com.google.gson.annotations.SerializedName

data class WelfareCenter(
    @SerializedName("name") var name: String,
    @SerializedName("type") var type: String,
    @SerializedName("road_address") var roadAddress: String?,
    @SerializedName("address") var address: String?,
    @SerializedName("telephone_number") var telephoneNumber: String,
    @SerializedName("latitude") var latitude: Double,
    @SerializedName("longitude") var longitude: Double,
)
