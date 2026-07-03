package com.company.carryon.data.network

actual object DeepLinkHandler {
    actual fun handle(uri: String) {
        emitStripeConnectDeepLink(uri)
    }
}
