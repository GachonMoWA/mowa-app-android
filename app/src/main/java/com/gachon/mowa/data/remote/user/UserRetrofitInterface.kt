package com.gachon.mowa.data.remote.user

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface UserRetrofitInterface {

    /**
     * 모든 사용자 목록을 가져옵니다.
     * @see UserService.getAll
     */
    @GET("/user/")
    fun getAll(
    ): Call<List<User>>

    /**
     * 사용자를 서버에 등록합니다.
     * @see UserService.addUser
     */
    @POST("/user/")
    fun addUser(
        @Body user: User
    ): Call<User>

    /**
     * 특정 사용자 정보를 가져옵니다.
     * @see UserService.getUserByUserId
     */
    @GET("/user/{user_id}")
    fun getUserByUserId(
        @Path("user_id") userId: String
    ): Call<ResponseBody>

    /**
     * 특정 사용자 정보를 업데이트합니다.
     * @see UserService.updateUserByUserId
     */
    @PUT("/user/{user_id}")
    fun updateUserByUserId(
        @Path("user_id") userId: String
    ): Call<ResponseBody>

    /**
     * 특정 사용자 정보를 삭제합니다.
     * @see UserService.deleteUserByUserId
     */
    @DELETE("/user/{user_id}")
    fun deleteUserByUserId(
        @Path("user_id") userId: String
    ): Call<ResponseBody>

    /**
     * 특정 사용자의 BSSID 값을 서버에 등록합니다.
     * @see UserService.addMacAddressByUserId
     */
    @PUT("/pi/{user_id}/{mac_address}")
    fun addMacAddressByUserId(
        @Path("user_id") userId: String,
        @Body user: User
    ): Call<User>

    /**
     * 특정 사용자의 라즈베리파이를 서버에 등록합니다.
     * @see UserService.addRaspberryPiByUserId
     */
    @PUT("/pi/{user_id}/{mac_address}/")
    fun addRaspberryPiByUserId(
        @Path("user_id") userId: String,
        @Path("mac_address") macAddress: String
    ): Call<ResponseBody>

    /**
     * 등록된 라즈베리파이가 있는지 체크합니다.
     * 200: 등록된 라즈베리파이가 존재하는 경우
     * 201: 연결된 와이파이에 파이가 있는 경우
     * 404: 등록된 라즈베리파이도 없고, 같은 BSSID를 가진 라즈베리파이도 없는 경우
     * @see UserService.checkRaspberryPiByUserId
     */
    @GET("pi/check/{user_id}")
    fun checkRaspberryPiByUserId(
        @Path("user_id") userId: String
    ): Call<ResponseBody>
}
