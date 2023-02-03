package com.gachon.mowa.util

import ai.api.AIDataService
import ai.api.android.AIConfiguration
import ai.api.model.AIRequest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Insets
import android.graphics.Point
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.gachon.mowa.BuildConfig
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.dialogflow.v2.SessionName
import com.google.cloud.dialogflow.v2.SessionsClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

class ApplicationClass : Application() {
    companion object {

        // Time (do not disturb)
        var startHour: Int = -1
        var startMinute: Int = -1
        var endHour: Int = -1
        var endMinute: Int = -1
        const val TIME_PICKER_INTERVAL = 10

        // Tag
        const val TAG_APP: String = "APP_MOWA"
        const val TAG_SCREEN_MODE: String = "SCREEN_MODE"
        const val TAG_USER_ID: String = "USER_ID"
        const val TAG_GUIDE_MODE: String = "GUIDE_MODE"
        const val TAG_USER_EMAIL: String = "USER_EMAIL"
        const val TAG_USER_NAME: String = "USER_NAME"
        const val TAG_LONGITUDE: String = "LOCATION_LONGITUDE"
        const val TAG_LATITUDE: String = "LOCATION_LATITUDE"

        // Permission
        const val PERMISSIONS_REQUEST_READ_LOCATION = 0x00000001

        // Alarm
        private const val NOTIFICATION_CHANNEL_ID = "MOWA_ALARM"
        private const val NOTIFICATION_CHANNEL_NAME = "MOWA"
        private const val NOTIFICATION_CHANNEL_DESCRIPTION =
            "for monitoring and sending information"

        // Database
        const val APP_DATABASE = "mowa"

        // SharedPreferences를 사용하기 위한
        lateinit var mSharedPreferences: SharedPreferences

        // Retrofit
        lateinit var retrofit: Retrofit
        private const val BASE_URL: String = BuildConfig.SERVER_BASE_URL
//        private const val BASE_URL: String = ""

        /* Dialogflow */

        // Specifying the access token, locale, and recognition engine
        val uuid = UUID.randomUUID().toString()
        lateinit var credentials: GoogleCredentials
        lateinit var sessionsClient: SessionsClient
        lateinit var sessionName: SessionName

//        lateinit var aiConfig: AIConfiguration
//        lateinit var aiDataService: AIDataService
//        lateinit var aiRequest: AIRequest

        // Toast message를 띄워줍니다.
        fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        /**
         * 디바이스 크기에 따라 사이즈 변경
         */
        fun WindowManager.currentWindowMetricsPointCompat(): Point {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowInsets = currentWindowMetrics.windowInsets
                var insets: Insets = windowInsets.getInsets(WindowInsets.Type.navigationBars())

                windowInsets.displayCutout?.run {
                    insets = Insets.max(
                        insets,
                        Insets.of(safeInsetLeft, safeInsetTop, safeInsetRight, safeInsetBottom)
                    )
                }

                val insetsWidth = insets.right + insets.left
                val insetsHeight = insets.top + insets.bottom
                Point(
                    currentWindowMetrics.bounds.width() - insetsWidth,
                    currentWindowMetrics.bounds.height() - insetsHeight
                )
            } else {
                Point().apply {
                    defaultDisplay.getSize(this)
                }
            }
        }

        /**
         * Intent를 사용하여 설정 앱 알림 화면으로 이동한다.
         */
        fun getNotificationIntent(packageName: String, uid: Int): Intent {
            val intent = Intent().apply {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }

            /*
            val intent = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    Intent().apply {
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    }
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                    Intent().apply {
                        action = "android.settings.APP_NOTIFICATION_SETTINGS"
                        putExtra("app_package", packageName)
                        putExtra("app_uid", uid)
                    }
                }

                else -> {
                    Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        addCategory(Intent.CATEGORY_DEFAULT)
                        data = Uri.parse("package:${packageName}")
                    }
                }
            }
             */

            return intent
        }

        /**
         * Notification을 사용하기 위해 NotificationChannel을 등록한다.
         */
        fun registerNotificationChannel(context: Context) {
            Log.d("NOTIFICATION", "registerNotificationChannel")

            /* 알림과 관련된 정보를 시스템에 등록한다. */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // 알림 우선 순위 설정
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    importance
                )
                    .apply {
                        description = NOTIFICATION_CHANNEL_DESCRIPTION
                    }

                // 만든 채널 정보를 시스템에 등록
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }


    }

    @Override
    override fun onCreate() {
        super.onCreate()

        /* HTTP 통신 (Retrofit) */

        // HTTP 통신을 위한 클라이언트 옵션
        val client: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(30000, TimeUnit.MILLISECONDS)  // Timeout 3초 설정
            .connectTimeout(30000, TimeUnit.MILLISECONDS)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        mSharedPreferences = applicationContext.getSharedPreferences(TAG_APP, Context.MODE_PRIVATE)
    }
}
