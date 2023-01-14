package com.gachon.mowa.data.local.phonebook

import androidx.room.*

@Dao
interface PhoneBookDao {
    @Insert
    fun insert(phoneBook: PhoneBook)

    @Update
    fun update(phoneBook: PhoneBook)

    @Delete
    fun delete(tphoneBook: PhoneBook)

    @Query("SELECT * FROM phone_book")
    fun getAll(): List<PhoneBook>

    // 체크 상태 업데이트
    @Query("UPDATE phone_book SET isChecked = :isChecked WHERE idx = :idx")
    fun updateIsChecked(idx: Int, isChecked: Int)

    // 원하는 체크 상태에 해당하는 값만 가져온다.
    @Query("SELECT * FROM phone_book WHERE isChecked = :isChecked")
    fun getAllByCheckStatus(isChecked: Boolean): List<PhoneBook>

    // 테이블에 있는 모든 값을 지운다.
    @Query("DELETE FROM phone_book")
    fun clearAll()
}
