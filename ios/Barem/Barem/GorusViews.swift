import SwiftUI

private struct GorusCategoryItem: Identifiable {
    let id: String
    let title: String
    let hasTopics: Bool
}

private struct GorusTopicItem: Identifiable {
    let id: String
    let title: String
}

struct GorusHubView: View {
    private let categories: [GorusCategoryItem] = [
        .init(id: "memur", title: "Memurlar", hasTopics: true),
        .init(id: "akademik", title: "Akademik Personel", hasTopics: false),
        .init(id: "sozlesmeli", title: "Sözleşmeli Personel", hasTopics: false),
        .init(id: "surekli", title: "Sürekli İşçiler", hasTopics: false),
        .init(id: "gecici", title: "Geçici İşçiler", hasTopics: false),
        .init(id: "diger", title: "Diğer Personel", hasTopics: false),
    ]

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    Text("Personel gruplarına göre mevzuat ve görüş yazıları.")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                    ChunkedTwoColumnGrid(items: categories, spacing: 12) { cat in
                        NavigationLink {
                            if cat.hasTopics {
                                GorusMemurTopicsView()
                            } else {
                                GorusComingSoonView(title: cat.title)
                            }
                        } label: {
                            BaremHomePrimaryCard(
                                title: cat.title,
                                subtitle: "Konuları görüntüle",
                                tint: .gray
                            )
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding()
            }
            .navigationTitle("Görüş")
        }
    }
}

struct GorusMemurTopicsView: View {
    private let topics: [GorusTopicItem] = [
        .init(id: "mali", title: "Mali Haklar"),
        .init(id: "sosyal", title: "Sosyal Haklar"),
        .init(id: "atama", title: "Atama"),
        .init(id: "izin", title: "İzin"),
        .init(id: "disiplin", title: "Disiplin"),
        .init(id: "yukselme", title: "Görevde Yükselme"),
        .init(id: "adaylik", title: "Adaylık"),
        .init(id: "vekalet", title: "Vekalet"),
        .init(id: "ekders", title: "Ek Ders"),
    ]

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text("Konu başlıklarına dokunarak içeriği açın.")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                ChunkedTwoColumnGrid(items: topics, spacing: 12) { topic in
                    NavigationLink {
                        GorusArticlePlaceholderView(title: topic.title)
                    } label: {
                        BaremHomePrimaryCard(
                            title: topic.title,
                            subtitle: "Yazı yakında eklenecek",
                            tint: .blue
                        )
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding()
        }
        .navigationTitle("Memurlar")
    }
}

struct GorusArticlePlaceholderView: View {
    let title: String
    var body: some View {
        Text("Bu başlık için web sitemizdeki yazı yakında uygulamada da yayınlanacak.")
            .padding()
            .navigationTitle(title)
    }
}

struct GorusComingSoonView: View {
    let title: String
    var body: some View {
        Text("Bu personel grubu için içerikler yakında eklenecek.")
            .padding()
            .navigationTitle(title)
    }
}
