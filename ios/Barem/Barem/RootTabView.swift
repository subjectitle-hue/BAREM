import SwiftUI

struct RootTabView: View {
    var body: some View {
        TabView {
            WelcomeView()
                .tabItem { Label("Hesap", systemImage: "house") }

            GorusHubView()
                .tabItem { Label("Görüş", systemImage: "text.book.closed") }

            HistoryView()
                .tabItem { Label("Geçmiş", systemImage: "clock") }

            GeneralStatisticsHubView()
                .tabItem { Label("Genel İstatistik", systemImage: "chart.bar") }
        }
    }
}
