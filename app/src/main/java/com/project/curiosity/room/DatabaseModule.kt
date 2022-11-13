package com.project.curiosity.room

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context : Context) : AppDataBase =
        Room.databaseBuilder(context, AppDataBase::class.java, "device").build()

    @Singleton
    @Provides
    fun provideDAO(appDataBase: AppDataBase) = appDataBase.DeviceDAO()
}