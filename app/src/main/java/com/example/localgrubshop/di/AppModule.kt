package com.example.localgrubshop.di

import android.content.Context
import android.content.SharedPreferences
import androidx.work.WorkManager
import com.example.localgrubshop.domain.repository.NotificationRepository
import com.example.localgrubshop.utils.NetworkUtils
import com.example.localgrubshop.utils.NotificationHelper
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
    fun provideNetworkUtils(@ApplicationContext context: Context): NetworkUtils {
        return NetworkUtils(context)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("GlowPointPrefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(): NotificationHelper {
        return NotificationHelper()
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(): NotificationRepository {
        return NotificationRepository()
    }


    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}