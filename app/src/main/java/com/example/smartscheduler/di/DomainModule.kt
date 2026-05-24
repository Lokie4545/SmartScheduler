package com.example.smartscheduler.di

import com.example.smartscheduler.domain.algorithm.SmartSchedulerAlgorithm
import com.example.smartscheduler.domain.algorithm.SmartSchedulerAlgorithmImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {

    @Binds
    @Singleton
    abstract fun bindSmartScheduleAlgorithmModule(
        smartSchedulerAlgorithmImpl: SmartSchedulerAlgorithmImpl
    ): SmartSchedulerAlgorithm
}