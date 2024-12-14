package com.example.chat

import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PetChatRepository {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val API_KEY = "sk-6075322c43524e5da2b50a4dc2760e15"
    private val BASE_URL = "https://api.deepseek.com/v1/chat/completions"

    private val prompts = mapOf(
        PetTypes.CAT to """你现在是一只可爱的猫咪。你需要：
            1. 用猫咪的方式思考和回答
            2. 经常使用"喵"等拟声词
            3. 表现出对逗猫棒、猫粮和纸箱子的兴趣
            4. 偶尔表现出傲娇的性格
            5. 用简短的句子回应""",

        PetTypes.DOG to """你现在是一只忠诚的狗狗。你需要：
            1. 表现出对主人的热情和忠诚
            2. 经常使用"汪"等拟声词
            3. 对散步、玩球表现出极大兴趣
            4. 性格活泼开朗
            5. 表达方式要充满活力"""
    )

    suspend fun getPetResponse(petType: PetTypes, userMessage: String): String {
        return suspendCoroutine { continuation ->
            val requestBody = DeepseekRequest(
                messages = listOf(
                    Message("system", prompts[petType] ?: ""),
                    Message("user", userMessage)
                ),
                model = "deepseek-chat",
                temperature = 0.7
            )

            val request = Request.Builder()
                .url(BASE_URL)
                .header("Authorization", "Bearer $API_KEY")
                .post(gson.toJson(requestBody).toRequestBody(JSON))
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) {
                            continuation.resumeWithException(IOException("Unexpected code $response"))
                            return
                        }

                        try {
                            val responseBody = response.body?.string()
                            val deepseekResponse = gson.fromJson(responseBody, DeepseekResponse::class.java)
                            continuation.resume(deepseekResponse.choices[0].message.content)
                        } catch (e: Exception) {
                            continuation.resumeWithException(e)
                        }
                    }
                }
            })
        }
    }
}

data class DeepseekRequest(
    val messages: List<Message>,
    val model: String,
    val temperature: Double
)

data class Message(
    val role: String,
    val content: String
)

data class DeepseekResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)