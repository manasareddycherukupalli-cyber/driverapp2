import UIKit
import SwiftUI
import ComposeApp
import CoreLocation
import Contacts
import AVFoundation
import Photos

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            .onAppear {
                PermissionBootstrapper.shared.requestInitialPermissionsIfNeeded()
            }
    }
}

final class PermissionBootstrapper: NSObject, CLLocationManagerDelegate {
    static let shared = PermissionBootstrapper()

    private let hasRequestedKey = "initial_permissions_requested"
    private let locationManager = CLLocationManager()
    private var pendingLocationCompletion: (() -> Void)?
    private var isRequestInFlight = false

    private override init() {
        super.init()
        locationManager.delegate = self
    }

    func requestInitialPermissionsIfNeeded() {
        guard !UserDefaults.standard.bool(forKey: hasRequestedKey) else { return }
        guard !isRequestInFlight else { return }

        isRequestInFlight = true
        UserDefaults.standard.set(true, forKey: hasRequestedKey)

        requestLocationPermission { [weak self] in
            self?.requestContactsPermission { [weak self] in
                self?.requestCameraPermission { [weak self] in
                    self?.requestPhotoLibraryPermission { [weak self] in
                        self?.isRequestInFlight = false
                    }
                }
            }
        }
    }

    private func requestLocationPermission(completion: @escaping () -> Void) {
        switch locationManager.authorizationStatus {
        case .notDetermined:
            pendingLocationCompletion = completion
            locationManager.requestWhenInUseAuthorization()
        default:
            completion()
        }
    }

    private func requestContactsPermission(completion: @escaping () -> Void) {
        let status = CNContactStore.authorizationStatus(for: .contacts)
        guard status == .notDetermined else {
            completion()
            return
        }

        CNContactStore().requestAccess(for: .contacts) { _, _ in
            DispatchQueue.main.async {
                completion()
            }
        }
    }

    private func requestCameraPermission(completion: @escaping () -> Void) {
        let status = AVCaptureDevice.authorizationStatus(for: .video)
        guard status == .notDetermined else {
            completion()
            return
        }

        AVCaptureDevice.requestAccess(for: .video) { _ in
            DispatchQueue.main.async {
                completion()
            }
        }
    }

    private func requestPhotoLibraryPermission(completion: @escaping () -> Void) {
        let status: PHAuthorizationStatus
        if #available(iOS 14, *) {
            status = PHPhotoLibrary.authorizationStatus(for: .readWrite)
        } else {
            status = PHPhotoLibrary.authorizationStatus()
        }

        guard status == .notDetermined else {
            completion()
            return
        }

        if #available(iOS 14, *) {
            PHPhotoLibrary.requestAuthorization(for: .readWrite) { _ in
                DispatchQueue.main.async {
                    completion()
                }
            }
        } else {
            PHPhotoLibrary.requestAuthorization { _ in
                DispatchQueue.main.async {
                    completion()
                }
            }
        }
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        guard let completion = pendingLocationCompletion else { return }
        pendingLocationCompletion = nil
        completion()
    }
}
