package com.suda.agent.core

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suda.agent.service.ConversationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class MainViewModel(
    private val conversationService: ConversationService
) : ViewModel() {

    val uiState: StateFlow<ConversationService.State> = conversationService.state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ConversationService.State.STATE_INIT_NOW
        )

    val initLogs: StateFlow<List<String>> = conversationService.initLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun onMicPressed() {
        conversationService.handleEvent(ConversationService.Event.MicPressed)
    }

    fun onMicReleased() {
        conversationService.handleEvent(ConversationService.Event.MicReleased)
    }

    suspend fun initializeApp() {
        conversationService.initializeApp()
    }


////


    // 좌석 정보 실시간 Flow
    private val _seatFlow = MutableStateFlow(0)
    val seatFlow: StateFlow<Int> = _seatFlow

    // WebSocket 객체 저장
    private var webSocket: WebSocket? = null

    // WebSocket 연결
    fun connectWebSocket(ip: String) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("ws://192.168.0.4:5555/ws")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WS", "connected")
                webSocket.send("""{"command":"hello"}""")  // ★ 첫 메시지
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val seatValue = text.toIntOrNull()
                if (seatValue != null) {
                    viewModelScope.launch {
                        _seatFlow.value = seatValue
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                t.printStackTrace()
            }
        })
    }

    // 서버로 좌석값 보내기
    fun updateSeat(num: Int) {
        webSocket?.send(num.toString())
    }

}