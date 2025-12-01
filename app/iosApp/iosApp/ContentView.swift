import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    private let component: IosUiComponent

    init(component: IosUiComponent) {
        self.component = component
    }

    func makeUIViewController(context: Context) -> UIViewController {
        component.uiViewControllerFactory()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    private let component: IosUiComponent

    init(component: IosUiComponent) {
        self.component = component
    }

    var body: some View {
        ComposeView(component: component)
            .ignoresSafeArea(.all, edges: .all)
    }
}
