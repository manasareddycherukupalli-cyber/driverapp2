package com.company.carryon.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.company.carryon.data.network.HttpClientFactory
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

@Serializable
private data class UpdateConfigResponse(val data: UpdateConfig)

@Serializable
private data class UpdateConfig(val minimumVersion: String, val storeUrl: String? = null)

private sealed interface UpdateState {
    data object Checking : UpdateState
    data object Allowed : UpdateState
    data class Required(val storeUrl: String?) : UpdateState
}

internal fun isVersionBelow(current: String, minimum: String): Boolean {
    val currentParts = current.split('.').map { it.toIntOrNull() ?: 0 }
    val minimumParts = minimum.split('.').map { it.toIntOrNull() ?: 0 }
    val length = maxOf(currentParts.size, minimumParts.size)
    return (0 until length).firstNotNullOfOrNull { index ->
        val comparison = (currentParts.getOrNull(index) ?: 0).compareTo(minimumParts.getOrNull(index) ?: 0)
        comparison.takeIf { it != 0 }
    }?.let { it < 0 } ?: false
}

@Composable
fun AppUpdateGate(content: @Composable () -> Unit) {
    var state by remember { mutableStateOf<UpdateState>(UpdateState.Checking) }

    LaunchedEffect(Unit) {
        state = try {
            val config = HttpClientFactory.client
                .get("/api/v1/app-config/minimum-version?app=driver&platform=${appPlatform()}") {
                    timeout { requestTimeoutMillis = 5_000 }
                }
                .body<UpdateConfigResponse>()
                .data
            if (isVersionBelow(appVersion(), config.minimumVersion)) {
                UpdateState.Required(config.storeUrl)
            } else {
                UpdateState.Allowed
            }
        } catch (_: Exception) {
            UpdateState.Allowed
        }
    }

    when (val current = state) {
        UpdateState.Checking -> Surface(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { CircularProgressIndicator() }
        }
        UpdateState.Allowed -> content()
        is UpdateState.Required -> MandatoryUpdateScreen(current.storeUrl)
    }
}

@Composable
private fun MandatoryUpdateScreen(storeUrl: String?) {
    val uriHandler = LocalUriHandler.current
    val updateUrl = storeUrl ?: fallbackStoreUrl()
    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Update required", style = MaterialTheme.typography.headlineMedium)
            Text(
                "A newer version of CarryOn Driver is required to continue.",
                modifier = Modifier.padding(vertical = 16.dp),
                textAlign = TextAlign.Center,
            )
            Button(onClick = { updateUrl?.let(uriHandler::openUri) }, enabled = updateUrl != null) {
                Text("Update app")
            }
        }
    }
}
