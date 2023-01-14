package com.gachon.mowa.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gachon.mowa.data.local.phonebook.PhoneBook
import com.gachon.mowa.data.local.phonebook.PhoneBookDao
import com.gachon.mowa.util.ApplicationClass.Companion.APP_DATABASE

@Database(entities = [PhoneBook::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun phoneBookDao(): PhoneBookDao

    companion object {
        private var instance: AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase? {
            if (instance == null) {
                synchronized(AppDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        APP_DATABASE
                    ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
                }
            }

            return instance
        }
    }
}
