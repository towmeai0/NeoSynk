package com.example.neosynk.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.neosynk.data.database.chatdatabase.ChatDao
import com.example.neosynk.data.database.chatdatabase.ChatDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context):ChatDatabase {
        return Room.databaseBuilder(
            context,
            ChatDatabase::class.java,
            "chat_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideChatDao(chatDatabase: ChatDatabase): ChatDao{
        return chatDatabase.chatDao()
    }
}