package com.gachon.mowa.dialogflow

import android.os.AsyncTask
import android.util.Log
import com.gachon.mowa.util.getUserEmail
import com.gachon.mowa.util.getUserName
import com.google.cloud.dialogflow.v2.DetectIntentRequest
import com.google.cloud.dialogflow.v2.DetectIntentResponse
import com.google.cloud.dialogflow.v2.QueryInput
import com.google.cloud.dialogflow.v2.QueryParameters
import com.google.protobuf.Struct
import com.google.protobuf.Value
import com.google.cloud.dialogflow.v2.SessionName
import com.google.cloud.dialogflow.v2.SessionsClient


/**
 * AsnycTask는 deprecated 되어서 잘 사용하진 않지만, dialogflow 테스트를 위해 임의로 사용
 */
class DialogflowRequestService(
    private val dialogflowResponseAPI: DialogflowResponseAPI,
    private val sessionName: SessionName,
    private val sessionsClient: SessionsClient,
    private val queryInput: QueryInput
) : AsyncTask<Void, Void, DetectIntentResponse>() {
    companion object {
        const val TAG = "SERVICE/MESSAGE"
    }

    override fun doInBackground(vararg p0: Void?): DetectIntentResponse? {
        try {
            var queryParametersBuilder=QueryParameters.newBuilder()
            var parameters=Struct.newBuilder()



            parameters.putFields("from",Value.newBuilder().setStringValue("AndroidMoWA").build())
            parameters.putFields("email",Value.newBuilder().setStringValue(getUserEmail()).build())
            parameters.putFields("name",Value.newBuilder().setStringValue(getUserName()).build())


            queryParametersBuilder.setPayload(parameters)

            val detectIntentRequest: DetectIntentRequest = DetectIntentRequest.newBuilder()
                .setSession(sessionName.toString())
                .setQueryInput(queryInput)
                .setQueryParams(queryParametersBuilder)
                .build()


            return sessionsClient.detectIntent(detectIntentRequest)

        } catch (e: java.lang.Exception) {
            Log.d(TAG, "doInBackground/e: $e")
            e.printStackTrace()
        }

        return null
    }

    override fun onPostExecute(result: DetectIntentResponse?) {
        super.onPostExecute(result)
        // handle return response here
        Log.d(TAG,"detectintentresponse/result: $result")
        dialogflowResponseAPI.dialogflowCallback(result!!)
    }
}
