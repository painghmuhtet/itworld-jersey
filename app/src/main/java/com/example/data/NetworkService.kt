package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@Serializable
data class AiChatTurn(
    val role: String,
    val content: String
)

@Serializable
private data class AiChatRequest(
    val messages: List<AiChatTurn>
)

@Serializable
private data class AiChatResponse(
    val reply: String? = null,
    val error: String? = null
)

@Serializable
private data class LiveJoinRequest(
    val name: String,
    val phone: String,
    val consent: Boolean
)

@Serializable
data class LiveJoinResponse(
    val ok: Boolean = false,
    val token: String = "",
    val name: String = "",
    val error: String? = null
)

@Serializable
data class LiveChatMessage(
    val id: Long = 0,
    val name: String = "Guest",
    val message: String = "",
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
private data class LiveMessagesResponse(
    val ok: Boolean = false,
    val messages: List<LiveChatMessage> = emptyList(),
    val error: String? = null
)

@Serializable
private data class LiveSendRequest(
    val message: String
)

@Serializable
private data class LiveSendResponse(
    val ok: Boolean = false,
    val id: Long = 0,
    val error: String? = null
)

class StoreApiService {

    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun sendAiChat(history: List<AiChatTurn>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val payload = json.encodeToString(AiChatRequest(history.takeLast(12)))
            val request = Request.Builder()
                .url(StoreConstants.AI_CHAT_API_URL)
                .header("Origin", "https://ai.itworldjersey.store")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                val parsed = runCatching { json.decodeFromString<AiChatResponse>(raw) }.getOrNull()
                if (resp.isSuccessful && !parsed?.reply.isNullOrBlank()) {
                    Result.success(parsed!!.reply!!)
                } else {
                    val msg = parsed?.error ?: "The AI service is temporarily unavailable (${resp.code})."
                    Result.failure(IllegalStateException(msg))
                }
            }
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException(
                    e.message ?: "AI connection unavailable. You can still contact the shop directly using WhatsApp."
                )
            )
        }
    }

    suspend fun joinLiveChat(
        name: String,
        phone: String,
        consent: Boolean
    ): Result<LiveJoinResponse> = withContext(Dispatchers.IO) {
        try {
            val payload = json.encodeToString(LiveJoinRequest(name.trim(), phone.trim(), consent))
            val request = Request.Builder()
                .url("${StoreConstants.LIVE_CHAT_API_BASE}/join")
                .header("Origin", "https://itworldjersey.store")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                val parsed = runCatching { json.decodeFromString<LiveJoinResponse>(raw) }.getOrNull()
                if (resp.isSuccessful && parsed != null && parsed.token.isNotBlank()) {
                    Result.success(parsed)
                } else {
                    val msg = parsed?.error ?: "Unable to join chat (${resp.code})."
                    Result.failure(IllegalStateException(msg))
                }
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException(e.message ?: "Chat is temporarily unavailable."))
        }
    }

    suspend fun fetchLiveMessages(afterId: Long): Result<List<LiveChatMessage>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${StoreConstants.LIVE_CHAT_API_BASE}/messages?after=$afterId")
                .header("Accept", "application/json")
                .header("Origin", "https://itworldjersey.store")
                .get()
                .build()

            client.newCall(request).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                val parsed = runCatching { json.decodeFromString<LiveMessagesResponse>(raw) }.getOrNull()
                if (resp.isSuccessful && parsed != null) {
                    Result.success(parsed.messages)
                } else {
                    val msg = parsed?.error ?: if (resp.code == 503) {
                        "Chat database is not configured yet."
                    } else {
                        "Could not load chat messages (${resp.code})."
                    }
                    Result.failure(IllegalStateException(msg))
                }
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException(e.message ?: "Chat is temporarily unavailable."))
        }
    }

    suspend fun sendLiveMessage(token: String, message: String): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val payload = json.encodeToString(LiveSendRequest(message.trim()))
            val request = Request.Builder()
                .url("${StoreConstants.LIVE_CHAT_API_BASE}/messages")
                .header("Authorization", "Bearer $token")
                .header("Origin", "https://itworldjersey.store")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                val parsed = runCatching { json.decodeFromString<LiveSendResponse>(raw) }.getOrNull()
                if (resp.isSuccessful && parsed != null) {
                    Result.success(parsed.id)
                } else {
                    val msg = parsed?.error ?: "Message could not be sent (${resp.code})."
                    Result.failure(IllegalStateException(msg))
                }
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException(e.message ?: "Message could not be sent."))
        }
    }
}
