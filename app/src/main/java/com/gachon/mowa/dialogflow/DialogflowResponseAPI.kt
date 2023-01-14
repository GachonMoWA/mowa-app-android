package com.gachon.mowa.dialogflow

import com.google.cloud.dialogflow.v2.DetectIntentResponse

interface DialogflowResponseAPI {
    fun dialogflowCallback(detectIntentResponse: DetectIntentResponse?)
}
