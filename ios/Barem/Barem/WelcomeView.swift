import SwiftUI

struct WelcomeView: View {
    private struct Tile: Identifiable {
        let id: String
        let title: String
        let subtitle: String
        let navigates: Bool
    }

    private let tiles: [Tile] = [
        .init(id: "maas", title: "Maaş", subtitle: "Memur maaşı hesapla", navigates: true),
        .init(id: "soz", title: "Sözleşmeli", subtitle: "Yakında", navigates: false),
        .init(id: "isci", title: "Kamu işçisi", subtitle: "Yakında", navigates: false),
        .init(id: "emekli", title: "Emekli", subtitle: "Önizleme", navigates: false),
        .init(id: "asgari", title: "Asgari ücretli", subtitle: "Yakında", navigates: false),
        .init(id: "harcirah", title: "Harcırah", subtitle: "Yakında", navigates: false),
        .init(id: "yurtdisi", title: "Yurtdışı aylığı", subtitle: "Yakında", navigates: false),
        .init(id: "diger", title: "Diğer maaşlar", subtitle: "Yakında", navigates: false),
    ]

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    Text("Aylık ve Yıllık Maaş Hesaplama")
                        .font(.title2.bold())
                    ChunkedTwoColumnGrid(items: tiles, spacing: 12) { tile in
                        Group {
                            if tile.navigates {
                                NavigationLink {
                                    MemurWizardView()
                                } label: {
                                    tileCard(tile)
                                }
                            } else {
                                tileCard(tile)
                            }
                        }
                        .buttonStyle(.plain)
                    }
                    NavigationLink {
                        SendikaPanelView()
                    } label: {
                        BaremHomePrimaryCard(
                            title: "Sendika paneli",
                            subtitle: "Ücretli sürüm",
                            tint: .orange
                        )
                    }
                    .buttonStyle(.plain)
                }
                .padding()
            }
            .navigationTitle("BAREM")
        }
    }

    private func tileCard(_ tile: Tile) -> some View {
        BaremHomePrimaryCard(title: tile.title, subtitle: tile.subtitle, tint: .blue)
    }
}
