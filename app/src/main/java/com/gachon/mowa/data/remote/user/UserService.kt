package com.gachon.mowa.data.remote.user

import android.util.Log
import com.gachon.mowa.util.ApplicationClass.Companion.retrofit
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: 전체적으로 Success 작업 수정해줘야 합니다.
class UserService {
    companion object {
        const val TAG: String = "SERVICE/USER"
    }

    private val userService: UserRetrofitInterface =
        retrofit.create(UserRetrofitInterface::class.java)

    /**
     * 모든 사용자 목록을 가져옵니다.
     * FIXME: JsonArray가 아닌 List 형식으로 받아와도 되는지 의문
     * @see UserRetrofitInterface.getAll
     */
    fun getAll(userView: UserView) {

        userService.getAll().enqueue(object : Callback<List<User>> {

            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (!response.isSuccessful) return

                // API 응답 성공한 경우
                when (response.code()) {
                    200 -> userView.onUserSuccess()
                    else -> userView.onUserFailure("데이터 불러오기 실패")
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.d(TAG, "getAll/onFailure/${t.message}")
                userView.onUserFailure("네트워크 오류")
            }

        })
    }

    /**
     * 사용자를 서버에 등록합니다.
     * @see UserRetrofitInterface.addUser
     */
    fun addUser(userView: UserView, user: User) {

        userService.addUser(user).enqueue(object : Callback<User> {

            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (!response.isSuccessful) return

                // API 응답 성공한 경우
                when (response.code()) {
                    // TODO: 이후 해당 사용자에 대한 활동 기록을 보내야 한다. (ActivityService 참고)
                    201 -> userView.onUserSuccess()
                    else -> userView.onUserFailure("데이터 불러오기 실패")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.d(TAG, "addUser/onFailure/${t.message}")
                userView.onUserFailure("네트워크 오류")
            }

        })
    }

    /**
     * 특정 사용자 정보를 가져옵니다.
     * @see UserRetrofitInterface.getUserByUserId
     */
    fun getUserByUserId(userView: UserView, userId: String) {

        userService.getUserByUserId(userId).enqueue(object : Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!response.isSuccessful) return

                // API 응답 성공한 경우
                when (response.code()) {
                    200 -> userView.onUserSuccess()
                    else -> userView.onUserFailure("데이터 불러오기 실패")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d(TAG, "getUserByUserId/onFailure/${t.message}")
                userView.onUserFailure("네트워크 오류")
            }

        })
    }

    /**
     * 특정 사용자 정보를 업데이트합니다.
     * @see UserRetrofitInterface.updateUserByUserId
     */
    fun updateUserByUserId(userView: UserView, userId: String) {

        userService.updateUserByUserId(userId).enqueue(object : Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!response.isSuccessful) return

                // API 응답 성공한 경우
                when (response.code()) {
                    200 -> userView.onUserSuccess()
                    else -> userView.onUserFailure("데이터 불러오기 실패")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d(TAG, "updateUserByUserId/onFailure/${t.message}")
                userView.onUserFailure("네트워크 오류")
            }

        })
    }

    /**
     * 특정 사용자 정보를 삭제합니다.
     * @see UserRetrofitInterface.deleteUserByUserId
     */
    fun deleteUserByUserId(userView: UserView, userId: String) {

        userService.deleteUserByUserId(userId).enqueue(object : Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!response.isSuccessful) return

                // API 응답 성공한 경우
                when (response.code()) {
                    204 -> userView.onUserSuccess()
                    else -> userView.onUserFailure("데이터 불러오기 실패")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d(TAG, "deleteUserByUserId/onFailure/${t.message}")
                userView.onUserFailure("네트워크 오류")
            }

        })
    }

    /**
     * 특정 사용자의 BSSID 값을 서버에 등록합니다.
     * @see UserRetrofitInterface.addMacAddressByUserId
     */
    fun addMacAddressByUserId(userView: UserView, userId: String, user: User) {

        userService.addMacAddressByUserId(userId, user).enqueue(object : Callback<User> {

            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (!response.isSuccessful) return

                // TODO: 세부 구현 필요
                // Mac address를 등록 완료한 경우
                userView.onUserSuccess()
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.d(TAG, "addMacAddressByUserId/onFailure/${t.message}")
                userView.onUserFailure("네트워크 오류")
            }
        })
    }

    /**
     * 특정 사용자/BSSID에 대한 라즈베리파이를 등록합니다.
     * @see UserRetrofitInterface.addRaspberryPiByUserId
     */
    fun addRaspberryPiByUserId(userView: UserView, userId: String, macAddress: String) {

        userService.addRaspberryPiByUserId(userId, macAddress)
            .enqueue(object : Callback<ResponseBody> {

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (!response.isSuccessful) return

                    // TODO: 세부 구현 필요
                    userView.onUserSuccess()
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d(TAG, "addRaspberryPiByUserId/onFailure/${t.message}")
                    userView.onUserFailure("네트워크 오류")
                }

            })
    }

    /**
     * 특정 사용자/BSSID에 대한 라즈베리파이가 있는지를 확인합니다.
     * @see UserRetrofitInterface.checkRaspberryPiByUserId
     */
    fun checkRaspberryPiByUserId(userView: UserView, userId: String) {

        userService.checkRaspberryPiByUserId(userId).enqueue(object : Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!response.isSuccessful) return


                when (response.code()) {
                    200 -> {
                        Log.d(TAG, "checkRaspberryPiByUserId/onResponse/등록된 라즈베리파이가 존재합니다.")
                        userView.onUserSuccess()    // TODO: response.code()를 인자로 넘겨줘야 합니다.
                    }
                    201 -> {
                        Log.d(
                            TAG,
                            "checkRaspberryPiByUserId/onResponse/동일한 와이파이에 연결된 라즈베리파이가 존재하지만 이미 등록됐습니다."
                        )
                    }
                    404 -> {
                        Log.d(
                            TAG,
                            "checkRaspberryPiByUserId/onResponse/동일한 와이파이에 연결된 라즈베리파이가 존재하지 않거나, 등록된 라즈베리파이도 존재하지 않습니다."
                        )
                        userView.onUserFailure("라즈베리파이 등록 실패")    // TODO: response.code()를 인자로 넘겨줘야 합니다.
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d(TAG, "checkRaspberryPiByUserId/onFailure/${t.message}")
                userView.onUserFailure("네트워크 오류")
            }
        })
    }
}
