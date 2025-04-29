package com.ayudevices.neosynkparent.di

import android.content.Context
import androidx.room.Room
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatDao
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatDatabase
import com.ayudevices.neosynkparent.data.database.chatdatabase.PendingIntentDao
import com.ayudevices.neosynkparent.data.database.milestonedatabase.MilestoneDatabase
import com.ayudevices.neosynkparent.data.database.chatdatabase.MilestoneDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Provide ChatDatabase instance
    @Provides
    @Singleton
    fun provideChatDatabase(@ApplicationContext context: Context): ChatDatabase {
        return Room.databaseBuilder(
            context,
            ChatDatabase::class.java,
            "chat_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    // Provide ChatDao instance
    @Provides
    fun provideChatDao(chatDatabase: ChatDatabase): ChatDao {
        return chatDatabase.chatDao()
    }

    // Provide PendingIntentDao instance
    @Provides
    fun providePendingIntentDao(chatDatabase: ChatDatabase): PendingIntentDao {
        return chatDatabase.pendingIntentDao()
    }

    // Provide MilestoneDatabase instance
    @Provides
    @Singleton
    fun provideMilestoneDatabase(@ApplicationContext context: Context): MilestoneDatabase {
        return Room.databaseBuilder(
            context,
            MilestoneDatabase::class.java,
            "milestone_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    // Provide MilestoneDao instance
    @Provides
    fun provideMilestoneDao(milestoneDatabase: MilestoneDatabase): MilestoneDao {
        return milestoneDatabase.milestoneDao()
    }
}
