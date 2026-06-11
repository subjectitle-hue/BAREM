import SwiftUI

@main
struct BaremApp: App {
    var body: some Scene {
        WindowGroup {
            RootTabView()
                .task {
                    await PremiumManager.shared.start()
                }
        }
    }
}
