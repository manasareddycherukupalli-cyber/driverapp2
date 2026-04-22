import SwiftUI

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    // Handle Supabase OAuth callback deep links
                    // The supabase-kt SDK automatically handles the URL
                    // when the Auth plugin is configured with scheme/host
                }
        }
    }
}
