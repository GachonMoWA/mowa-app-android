package com.gachon.mowa.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * 화면 크기 조절을 위한 뷰 모델
 *
 * 0: Default Mode
 * 1: Large Mode
 */
class ScreenViewModel : ViewModel() {
    private var _mode = MutableLiveData<Int>()
    val mode get() = _mode

    init {
        _mode.value = 0
    }

    fun setMode(mode: Int) {
        _mode.value = mode
    }
}
