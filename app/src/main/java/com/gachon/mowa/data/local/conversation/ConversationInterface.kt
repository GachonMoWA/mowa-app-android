package com.gachon.mowa.data.local.conversation

import androidx.lifecycle.LiveData

interface ConversationInterface {

    // 새로 추가된 대화 목록 반영하기
    fun getRecentConversation(): LiveData<List<Conversation>>
}