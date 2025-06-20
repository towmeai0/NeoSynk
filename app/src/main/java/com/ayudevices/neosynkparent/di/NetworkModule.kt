package com.ayudevices.neosynkparent.di

import android.content.Context
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatDao
import com.ayudevices.neosynkparent.data.network.ApiService
import com.ayudevices.neosynkparent.data.network.ChatApiService
import com.ayudevices.neosynkparent.data.network.FcmApiService
import com.ayudevices.neosynkparent.data.network.TokenSender
import com.ayudevices.neosynkparent.data.repository.AuthRepository
import com.ayudevices.neosynkparent.data.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule{
    private const val BASE_URL = "http://65.0.115.229:3000/api/"

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): FcmApiService {
        return retrofit.create(FcmApiService::class.java)
    }

    @Provides
    @Singleton
    fun providepiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideChatApiService(retrofit: Retrofit): ChatApiService {
        return retrofit.create(ChatApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideTokenSender(
        fcmApiService: FcmApiService,
        chatDao: ChatDao,
        chatApiService: ChatApiService,
        @ApplicationContext context: Context,
        authRepository: AuthRepository,
        chatRepositoryProvider: Provider<ChatRepository> // <-- Add this
    ): TokenSender {
        return TokenSender(
            fcmApiService = fcmApiService,
            chatDao = chatDao,
            chatApiService = chatApiService,
            context = context,
            authRepository = authRepository,
            chatRepositoryProvider = chatRepositoryProvider // <-- Pass it here
        )
    }


    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface TokenSenderEntryPoint {
        fun tokenSender(): TokenSender
        fun apiService(): ApiService
    }
}