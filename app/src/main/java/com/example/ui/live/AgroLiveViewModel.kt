package com.example.ui.live

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.InlineData
import com.google.firebase.ai.type.LiveSession
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.liveGenerationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

data class AgroLiveUiState(
    val connecting: Boolean = false,
    val connected: Boolean = false,
    val microphoneEnabled: Boolean = false,
    val cameraEnabled: Boolean = false,
    val error: String? = null
)

@OptIn(PublicPreviewAPI::class)
class AgroLiveViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AgroLiveUiState())
    val uiState: StateFlow<AgroLiveUiState> = _uiState.asStateFlow()
    private var liveSession: LiveSession? = null

    fun connect(language: String = "ar") {
        if (_uiState.value.connecting || _uiState.value.connected) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(connecting = true, error = null)
            try {
                val model = Firebase.ai(
                    backend = GenerativeBackend.googleAI(),
                    useLimitedUseAppCheckTokens = true
                ).liveModel(
                    modelName = "gemini-3.1-flash-live-preview",
                    generationConfig = liveGenerationConfig {
                        responseModality = ResponseModality.AUDIO
                    }
                )
                val session = model.connect()
                liveSession = session
                val languageInstruction = when (language) {
                    "en" -> "Respond only in clear, natural English. Keep all explanations, labels and spoken replies in English."
                    "fr" -> "Répondez uniquement en français clair et naturel. Gardez toutes les explications et réponses vocales en français."
                    else -> "تحدث بالعربية الفصحى أو الدارجة الجزائرية حسب أسلوب المستخدم، وأجب بالعربية."
                }
                session.send(
                    "You are the AgroPro agricultural assistant. $languageInstruction " +
                        "Be friendly, concise and accurate. Focus on agriculture, plant diagnosis and practical advice. " +
                        "Never switch languages unless the user explicitly asks. If uncertain about a plant diagnosis, " +
                        "do not make a definite claim; request a clearer photo or more details."
                )
                session.startAudioConversation()
                _uiState.value = _uiState.value.copy(
                    connecting = false,
                    connected = true,
                    microphoneEnabled = true,
                    error = null
                )
            } catch (error: Exception) {
                liveSession = null
                _uiState.value = _uiState.value.copy(
                    connecting = false,
                    connected = false,
                    microphoneEnabled = false,
                    error = error.localizedMessage ?: "تعذر بدء المحادثة المباشرة."
                )
            }
        }
    }

    fun setMicrophoneEnabled(enabled: Boolean) {
        val session = liveSession ?: return
        if (!enabled) {
            session.stopAudioConversation()
            _uiState.value = _uiState.value.copy(microphoneEnabled = false)
        } else {
            viewModelScope.launch {
                try {
                    session.startAudioConversation()
                    _uiState.value = _uiState.value.copy(microphoneEnabled = true, error = null)
                } catch (error: Exception) {
                    _uiState.value = _uiState.value.copy(
                        microphoneEnabled = false,
                        error = error.localizedMessage ?: "تعذر تشغيل الميكروفون."
                    )
                }
            }
        }
    }

    fun setCameraEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(cameraEnabled = enabled)
    }

    fun sendVideoFrame(bitmap: Bitmap) {
        if (!_uiState.value.connected || !_uiState.value.cameraEnabled) return
        viewModelScope.launch {
            try {
                val scale = minOf(1f, 768f / maxOf(bitmap.width, bitmap.height))
                val scaled = if (scale < 1f) {
                    Bitmap.createScaledBitmap(
                        bitmap,
                        (bitmap.width * scale).toInt().coerceAtLeast(1),
                        (bitmap.height * scale).toInt().coerceAtLeast(1),
                        true
                    )
                } else bitmap
                val output = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 78, output)
                liveSession?.sendVideoRealtime(InlineData(output.toByteArray(), "image/jpeg"))
                if (scaled !== bitmap) scaled.recycle()
            } catch (_: Exception) {
                // A missed frame should not interrupt the audio conversation.
            }
        }
    }

    fun endSession() {
        liveSession?.let { session ->
            try {
                session.stopAudioConversation()
                viewModelScope.launch { runCatching { session.close() } }
            } catch (_: Exception) {
                // Session may already be closed by the server.
            }
        }
        liveSession = null
        _uiState.value = AgroLiveUiState()
    }

    override fun onCleared() {
        endSession()
        super.onCleared()
    }
}
