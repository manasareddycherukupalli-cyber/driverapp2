package com.company.carryon.data.network

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import kotlin.time.Duration.Companion.seconds

object SupabaseConfig {
    // Web Client ID from Google Cloud Console (OAuth 2.0 > Web application)
    const val GOOGLE_WEB_CLIENT_ID = "955810867262-ttbg70n8ttmp8jb3vv1he6abr9qkrn06.apps.googleusercontent.com"

    // Deep link scheme for iOS OAuth callback
    const val DEEP_LINK_SCHEME = "com.company.carryon"
    const val REDIRECT_URL = "$DEEP_LINK_SCHEME://login-callback"

    val client = createSupabaseClient(
        supabaseUrl = "https://liwhjhkqlwufnbekegas.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imxpd2hqaGtxbHd1Zm5iZWtlZ2FzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzE3ODM1OTYsImV4cCI6MjA4NzM1OTU5Nn0.kRFk1p3S4C6wQwKgkh5zhN3hJ_r2EfkEPqsvQ_tPvhg"
    ) {
        // Default is 10s in supabase-kt; OTP requests can exceed that on slower local/dev networks.
        requestTimeout = 30.seconds

        install(Auth) {
            flowType = FlowType.IMPLICIT
            // Keep OTP email auth free of redirect_to. A global scheme/host here makes
            // supabase-kt attach redirect_to to OTP calls, which fails validation in
            // projects that don't allow this deep-link URL for email OTP.
            // OAuth can still pass an explicit redirect URL at call sites if needed.
        }
        install(ComposeAuth) {
            googleNativeLogin(serverClientId = GOOGLE_WEB_CLIENT_ID)
        }
        install(Storage)
        install(Realtime)
    }
}
