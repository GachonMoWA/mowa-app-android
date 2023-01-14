package com.gachon.mowa.dialogflow

import android.os.AsyncTask
import android.util.Log
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


            //테스트용, 실제 사용자의 구글 계정 이메일과 이름을 가져와 사용할 것
            parameters.putFields("from",Value.newBuilder().setStringValue("AndroidMoWA").build())
            parameters.putFields("email",Value.newBuilder().setStringValue("gachon.mowa@gmail.com").build())
            parameters.putFields("name",Value.newBuilder().setStringValue("androidMowaTester").build())


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
