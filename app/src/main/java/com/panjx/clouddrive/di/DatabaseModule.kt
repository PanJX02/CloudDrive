package com.panjx.clouddrive.di

import android.content.Context
import com.panjx.clouddrive.data.database.AppDatabase
import com.panjx.clouddrive.data.database.TransferDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }
    
    @Provides
    fun provideTransferDao(database: AppDatabase): TransferDao {
        return database.transferDao()
    }
} 