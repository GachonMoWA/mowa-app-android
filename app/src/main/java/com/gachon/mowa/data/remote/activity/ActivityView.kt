package com.gachon.mowa.data.remote.activity

interface ActivityView {
    fun onActivitySuccess()
    fun onActivityFailure(message: String)
}

interface ActivityStatsView {
    fun onActivityStatsSuccess(activityStats: ActivityStats)
    fun onActivityStatsFailure(message: String)
}
