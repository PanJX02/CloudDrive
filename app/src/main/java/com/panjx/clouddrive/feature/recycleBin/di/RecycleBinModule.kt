package com.panjx.clouddrive.feature.recycleBin.di

import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
import com.panjx.clouddrive.feature.recycleBin.RecycleBinRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RecycleBinModule {
    
    @Provides
    @Singleton
    fun provideRecycleBinRepository(
        networkDatasource: MyRetrofitDatasource
    ): RecycleBinRepository {
        return RecycleBinRepository(networkDatasource)
    }
} 