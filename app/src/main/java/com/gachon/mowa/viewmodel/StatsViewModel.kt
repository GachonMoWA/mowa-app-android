package com.gachon.mowa.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gachon.mowa.data.remote.activity.ActivityStats
import com.gachon.mowa.util.getUserEmail
import com.gachon.mowa.util.getUserId

/**
 * 서버로부터 받아오는 유저의 활동 통계 데이터를 저장합니다.
 * 업데이트가 있는 경우, LiveData로 뷰에 해당 내용을 반영합니다.
 */
class StatsViewModel : ViewModel() {
    private var _stats = MutableLiveData<ActivityStats>()
    val stats get() = _stats

    init {
        _stats.value = ActivityStats(getUserEmail()!!, 0, 0, 0, 0)
    }

    fun setStats(activityStats: ActivityStats) {
        _stats.value = activityStats
    }
}
