package com.example.di

import com.example.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepository: FirebaseAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAssetRepository(
        assetRepository: FirebaseAssetRepository
    ): AssetRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        dashboardRepository: FirebaseDashboardRepository
    ): DashboardRepository

    @Binds
    @Singleton
    abstract fun bindActivityRepository(
        activityRepository: FirebaseActivityRepository
    ): ActivityRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepository: FirebaseNotificationRepository
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindMaintenanceRepository(
        maintenanceRepository: FirebaseMaintenanceRepository
    ): MaintenanceRepository
}
