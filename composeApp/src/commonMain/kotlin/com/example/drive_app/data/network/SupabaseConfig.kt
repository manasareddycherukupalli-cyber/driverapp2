package com.example.drive_app.data.network

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseConfig {
    val client = createSupabaseClient(
        supabaseUrl = "https://liwhjhkqlwufnbekegas.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imxpd2hqaGtxbHd1Zm5iZWtlZ2FzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzE3ODM1OTYsImV4cCI6MjA4NzM1OTU5Nn0.kRFk1p3S4C6wQwKgkh5zhN3hJ_r2EfkEPqsvQ_tPvhg"
    ) {
        install(Auth) {
            flowType = FlowType.IMPLICIT
        }
        install(Storage)
        install(Realtime)
    }
}
