import UIKit
import UserNotifications
import ComposeApp
#if canImport(FirebaseCore)
import FirebaseCore
#endif
#if canImport(FirebaseMessaging)
import FirebaseMessaging
#endif

private let pushTokenDefaultsKey = "push_token"
private let pendingIncomingJobDefaultsKey = "pending_incoming_job"

final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        configureFirebaseIfAvailable()
        requestNotificationAuthorization(application: application)

        if let userInfo = launchOptions?[.remoteNotification] as? [AnyHashable: Any] {
            persistIncomingJob(userInfo: userInfo)
        }
        return true
    }

    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        #if canImport(FirebaseMessaging)
        Messaging.messaging().apnsToken = deviceToken
        #endif
    }

    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        print("[Push] Failed to register for remote notifications: \(error)")
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound, .list])
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        persistIncomingJob(userInfo: response.notification.request.content.userInfo)
        completionHandler()
    }

    private func requestNotificationAuthorization(application: UIApplication) {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, error in
            if let error {
                print("[Push] Notification authorization error: \(error)")
            }
            guard granted else { return }
            DispatchQueue.main.async {
                application.registerForRemoteNotifications()
            }
        }
    }

    private func persistIncomingJob(userInfo: [AnyHashable: Any]) {
        let type = (userInfo["type"] as? String)?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        guard type == "JOB_REQUEST" else { return }

        UserDefaults.standard.set(true, forKey: pendingIncomingJobDefaultsKey)
        IncomingJobSignal.shared.signalIncomingJob()
    }

    private func configureFirebaseIfAvailable() {
        #if canImport(FirebaseCore) && canImport(FirebaseMessaging)
        guard let plistPath = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist"),
              let options = FirebaseOptions(contentsOfFile: plistPath)
        else {
            print("[Push] GoogleService-Info.plist not found; Firebase Messaging disabled on iOS.")
            return
        }

        if FirebaseApp.app() == nil {
            FirebaseApp.configure(options: options)
        }
        Messaging.messaging().delegate = self
        #else
        print("[Push] Firebase iOS SDK not linked; Firebase Messaging disabled on iOS.")
        #endif
    }
}

#if canImport(FirebaseMessaging)
extension AppDelegate: MessagingDelegate {
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        guard let fcmToken, !fcmToken.isEmpty else { return }
        UserDefaults.standard.set(fcmToken, forKey: pushTokenDefaultsKey)
    }
}
#endif
