package com.ayudevices.neosynkparent.data.repository

import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatDao
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val chatDao: ChatDao,
    //private val apiService: ChatApiService
) {

    suspend fun sendMessage(message: String){
        val userMsg = ChatEntity(
            message = message,
            sender = "user"
        )
        chatDao.insertMessage(userMsg)

        //replace with retrofit later
        delay(1000)
        val replyMsg = ChatEntity(
            message = when {
                message.contains("hi", true) -> "Hey there!"
                else -> "I'm still learning"
            },
            sender = "bot"
        )

        chatDao.insertMessage(replyMsg)
    }

    fun getAllMessages(): Flow<List<ChatEntity>> = chatDao.getAllMessages()
}
