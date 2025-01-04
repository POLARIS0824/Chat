package com.example.chat

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chat.data.ChatDatabase
import com.example.chat.model.ChatMessage
import com.example.chat.model.PetTypes
import com.example.chat.model.PictureInfo
import kotlinx.coroutines.launch

/**
 * 宠物聊天的ViewModel
 * 负责管理UI状态和处理用户交互
 */
class PetChatViewModel(application: Application) : AndroidViewModel(application) {
    // 初始化Repository，传入数据库DAO
    private val repository: PetChatRepository = PetChatRepository.getInstance(
        ChatDatabase.getDatabase(application).chatDao()
    )

    // 当前选择的宠物类型，默认为猫咪
    var currentPetType by mutableStateOf(PetTypes.CAT)
        private set

    // 聊天历史记录列表
    var chatHistory by mutableStateOf<List<ChatMessage>>(emptyList())
        private set

    // 最后一次AI返回的图片信息
    private var lastPictureInfo: PictureInfo? = null
        private set

    // 添加加载状态
    var isLoading by mutableStateOf(false)
        private set

    /**
     * 切换当前的宠物类型
     */
    fun selectPetType(petType: PetTypes) {
        currentPetType = petType
    }

    /**
     * 发送新消息
     * 处理用户输入，获取AI响应，并更新UI状态
     */
    fun sendMessage(content: String) {
        viewModelScope.launch {
            // 添加并保存用户消息
            val userMessage = ChatMessage(content, true)
            chatHistory = chatHistory + userMessage
            repository.saveChatMessage(userMessage, currentPetType)

            try {
                // 获取AI响应和图片信息
                val (response, pictureInfo) = repository.getPetResponseWithPictureInfo(currentPetType, content)
                val petMessage = ChatMessage(response, false)
                chatHistory = chatHistory + petMessage
                lastPictureInfo = pictureInfo
                // 保存AI响应
                repository.saveChatMessage(petMessage, currentPetType)
                
                // 处理累积的聊天记录
                repository.processUnprocessedChats()
            } catch (e: Exception) {
                // 处理错误情况
                val errorMessage = ChatMessage("抱歉，我现在有点累了...", false)
                chatHistory = chatHistory + errorMessage
                lastPictureInfo = null
                repository.saveChatMessage(errorMessage, currentPetType)
            }
        }
    }

    /**
     * 获取并清除最后的图片信息
     * 使用后即清除，确保图片信息只被使用一次
     */
    fun consumeLastPictureInfo(): PictureInfo? {
        val info = lastPictureInfo
        lastPictureInfo = null
        return info
    }
}