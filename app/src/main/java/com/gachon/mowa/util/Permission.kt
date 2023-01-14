package com.gachon.mowa.util

import android.content.Context
import androidx.core.app.NotificationManagerCompat

/**
 * 알림 권한 허용 여부를 확인한다.
 * 즉, 사용자가 설정에서 앱 알림을 켰는지에 대한 값을 true, false로 리턴한다.
 */
fun isAlarmPermission(context: Context): Boolean {
    return NotificationManagerCompat.from(context).areNotificationsEnabled()
}
