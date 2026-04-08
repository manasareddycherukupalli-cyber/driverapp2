# Copilot Instructions for `driverapp2` (CarryOn Driver)

## Architecture at a glance
- This is a Kotlin Multiplatform (KMP) app: shared business/UI code in `composeApp/src/commonMain`, platform bridges in `androidMain` and `iosMain`.
- UI is Compose Multiplatform with a custom navigator (`AppNavigator`) instead of Navigation Compose. Route state/back stack live in-memory (`presentation/navigation/AppNavigator.kt`).
- Dependency wiring is a manual service locator (`di/ServiceLocator`), not DI frameworks.
- Data flow is `ViewModel -> Repository -> Api interface -> Real*Api (Ktor) -> backend`.

## Auth/session model (important)
- OTP send/verify is handled client-side via Supabase SDK; backend auth calls are `sync`/`register` (`data/api/RealAuthApi.kt`).
- API auth must be attached per-request with `withAuth()`; do not rely on global auth headers (`data/network/HttpClientFactory.kt`).
- `HttpClientFactory` throws `AuthenticationException` on HTTP 401; Home dashboard code attempts session recovery via `authRepository.syncDriver()`.
- Many repository/API signatures still accept `driverId`, but current backend derives driver from JWT and callers pass `""` intentionally (`data/repository/AppRepository.kt`).

## Navigation and screen flow conventions
- App starts at Splash, then navigation is controlled manually through `navigateTo`, `navigateAndClearStack`, and `switchTab`.
- Post-auth is intentionally gated through `LocationPermission -> Document/Vehicle/Verification` before Home (`AuthViewModel.determinePostLocationScreen`).
- `SplashScreen` currently has `FORCE_PRE_HOME_FLOW_ON_LAUNCH = true`; onboarding is forced on launch unless this flag is changed.

## Realtime + push integration pattern
- Incoming jobs use a hybrid strategy:
  - Supabase Realtime listener on `Booking` (`data/network/RealtimeJobService.kt`)
  - Fallback polling every 30s (`HomeViewModel.startJobPolling()`)
  - Android FCM push sets `IncomingJobSignal.pendingCheck` for immediate poll (`androidMain/.../FcmService.kt`)
- Support chat also uses Supabase Realtime (`RealtimeChatService`) and then refreshes messages via REST.

## Platform-specific implementation rules
- Keep shared contracts in `commonMain` as `expect`, platform logic in `androidMain`/`iosMain` as `actual` (e.g., `MapView`, `LocationProvider`, `TokenStorage`, `ApiConfig`).
- `MainActivity` initializes token + location providers and notification channels; avoid bypassing these initializers.
- iOS entry is SwiftUI wrapper -> `MainViewController()` (`iosApp/iosApp/ContentView.swift`, `composeApp/src/iosMain/.../MainViewController.kt`).

## Build/run workflows used in this repo
- Android debug build: `./gradlew :composeApp:assembleDebug`
- Shared tests: `./gradlew :composeApp:allTests` (currently minimal sample test coverage)
- iOS: open `iosApp/iosApp.xcodeproj` in Xcode and run.
- Google services plugin is conditionally applied only when `google-services.json` exists in `composeApp` (`composeApp/build.gradle.kts`).
- Android maps key comes from `local.properties` (`GOOGLE_MAPS_API_KEY`) and is injected via manifest placeholders.

## Editing guidance for AI agents
- Preserve current layering and avoid introducing new navigation/DI frameworks unless explicitly requested.
- Reuse `UiState` + `StateFlow` patterns in ViewModels; surface transient errors via toast/shared-flow pattern where existing code does.
- When adding API calls, follow existing `ApiResponse<T>` parsing and `runCatching` style in `Real*Api` classes.
- Keep behavior parity across Android/iOS when modifying `expect/actual` APIs.
