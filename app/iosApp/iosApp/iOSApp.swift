import SwiftUI
import Shared


class AppDelegate: UIResponder, UIApplicationDelegate {
    var appComponent: IosAppComponent!
    
    func application(
            _ application: UIApplication,
            didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
        ) -> Bool {

            // Initialize the app component
            appComponent = createAppComponent(application: application)

            return true
        }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            let uiComponent = createUiComponent(appComponent: delegate.appComponent)
            ContentView(component: uiComponent)
        }
    }
}

private func createAppComponent(application: UIApplication) -> IosAppComponent {
    let component = IosComponentCreator.shared.createAppComponent(app: application)
    IosComponentHolder.shared.addComponent(component: component)
    return component
}

private func createUiComponent(appComponent: IosAppComponent) -> IosUiComponent {
    let component = IosComponentCreator.shared.createUiComponent(appComponent: appComponent)
    IosComponentHolder.shared.addComponent(component: component)
    return component
}
