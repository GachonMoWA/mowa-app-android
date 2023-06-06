package com.gachon.mowa.data.remote.dialogflow

import android.util.Log
import com.gachon.mowa.BuildConfig
import com.gachon.mowa.data.local.phonebook.PhoneBook
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FirstResponderService {

    companion object {
        const val TAG = "SERVICE/FIREBASE"
    }

    val database = Firebase.database(BuildConfig.FIREBASE_BASE_URL)

    fun updateFirstResponder(userId : String, firstResponder : List<FirstResponder>){
        val ref = database.getReference("dialogflow/${userId}/first_responder")
        ref.setValue(firstResponder)
    }

}