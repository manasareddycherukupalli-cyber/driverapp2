package com.company.carryon.di

import com.company.carryon.data.api.*
import com.company.carryon.data.network.DriverDocumentsStorage
import com.company.carryon.data.repository.*

object ServiceLocator {

    // ---- API Services (real Ktor implementations) ----
    private val authApi: AuthApi by lazy { RealAuthApi() }
    private val jobApi: JobApi by lazy { RealJobApi() }
    private val earningsApi: EarningsApi by lazy { RealEarningsApi() }
    private val ratingsApi: RatingsApi by lazy { RealRatingsApi() }
    private val supportApi: SupportApi by lazy { RealSupportApi() }
    private val notificationsApi: NotificationsApi by lazy { RealNotificationsApi() }
    val driverOnboardingApi: DriverOnboardingApi by lazy { DriverOnboardingApi() }
    val driverDocumentsStorage: DriverDocumentsStorage by lazy { DriverDocumentsStorage() }

    // ---- Repositories ----
    val authRepository: AuthRepository by lazy { AuthRepositoryImpl(authApi) }
    val jobRepository: JobRepository by lazy { JobRepositoryImpl(jobApi) }
    val earningsRepository: EarningsRepository by lazy { EarningsRepositoryImpl(earningsApi) }
    val ratingsRepository: RatingsRepository by lazy { RatingsRepositoryImpl(ratingsApi) }
    val supportRepository: SupportRepository by lazy { SupportRepositoryImpl(supportApi) }
    val notificationsRepository: NotificationsRepository by lazy { NotificationsRepositoryImpl(notificationsApi) }
}
