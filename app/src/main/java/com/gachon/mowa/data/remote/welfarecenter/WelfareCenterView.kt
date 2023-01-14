package com.gachon.mowa.data.remote.welfarecenter

interface WelfareCenterView {
    fun onGetWelfareCenterSuccess(welfareCenters: List<WelfareCenter>)
    fun onGetWelfareCenterFailure(code: Int, message: String)
}
