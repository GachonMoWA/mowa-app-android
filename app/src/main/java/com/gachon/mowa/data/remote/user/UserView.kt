package com.gachon.mowa.data.remote.user

interface UserView {
    fun onUserSuccess()
    fun onUserFailure(message: String)
}
