package com.example.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PetChatViewModel : ViewModel() {
    private val repository = PetChatRepository()

    var currentPetType by mutableStateOf(PetTypes.CAT)
        private set

    var chatHistory by mutableStateOf<List<ChatMessage>>(emptyList())
        private set

    fun selectPetType(petType: PetTypes) {
        currentPetType = petType
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            // 添加用户消息
            chatHistory = chatHistory + ChatMessage(content, true)

            try {
                // 获取宠物回复
                val response = repository.getPetResponse(currentPetType, content)
                chatHistory = chatHistory + ChatMessage(response, false)
            } catch (e: Exception) {
                // 处理错误
                chatHistory = chatHistory + ChatMessage("抱歉，我现在有点累了...", false)
            }
        }
    }
}

enum class PetTypes(val displayName: String) {
    CAT("猫咪"),
    DOG("狗狗")
}

data class ChatMessage(
    val content: String,
    val isFromUser: Boolean
)