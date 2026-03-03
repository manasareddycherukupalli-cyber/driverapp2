package com.example.drive_app.di

import com.example.drive_app.data.api.*
import com.example.drive_app.data.repository.*

/**
 * Simple service locator for dependency injection.
 * In production, replace with Koin or similar DI framework.
 *
 * Usage:
 *   val authRepo = ServiceLocator.authRepository
 */
object ServiceLocator {

    // ---- API Services (swap with real implementations for production) ----
    private val authApi: AuthApi by lazy { DummyAuthApi() }
    private val jobApi: JobApi by lazy { DummyJobApi() }
    private val earningsApi: EarningsApi by lazy { DummyEarningsApi() }
    private val ratingsApi: RatingsApi by lazy { DummyRatingsApi() }
    private val supportApi: SupportApi by lazy { DummySupportApi() }
    private val notificationsApi: NotificationsApi by lazy { DummyNotificationsApi() }

    // ---- Repositories ----
    val authRepository: AuthRepository by lazy { AuthRepositoryImpl(authApi) }
    val jobRepository: JobRepository by lazy { JobRepositoryImpl(jobApi) }
    val earningsRepository: EarningsRepository by lazy { EarningsRepositoryImpl(earningsApi) }
    val ratingsRepository: RatingsRepository by lazy { RatingsRepositoryImpl(ratingsApi) }
    val supportRepository: SupportRepository by lazy { SupportRepositoryImpl(supportApi) }
    val notificationsRepository: NotificationsRepository by lazy { NotificationsRepositoryImpl(notificationsApi) }
}
