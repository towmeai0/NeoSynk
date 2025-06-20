package com.ayudevices.neosynkparent.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.ayudevices.neosynkparent.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val players = mutableMapOf<String, ExoPlayer>()

    fun getPlayer(context: Context, url: String): ExoPlayer {
        return players.getOrPut(url) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(url))
                prepare()
                playWhenReady = true
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        players.values.forEach { it.release() }
        players.clear()
    }

    val messages = repository.getAllMessages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    fun onSendMessage(text: String) {
        viewModelScope.launch {
            repository.sendMessage(text)
        }
    }

    // Helper function to handle option button clicks
    fun onOptionSelected(option: String) {
        viewModelScope.launch {
            // Simply send the selected option as a message
            // The repository will handle marking messages as answered
            onSendMessage(option)
        }
    }
}
