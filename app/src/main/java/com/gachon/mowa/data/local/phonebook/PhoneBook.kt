package com.gachon.mowa.data.local.phonebook

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "phone_book")
data class PhoneBook(
    @SerializedName("name") val name: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("is_checked") var isChecked: Int = 0,
) {
    @PrimaryKey(autoGenerate = true)
    @SerializedName("index")
    var idx: Int = 0
}
