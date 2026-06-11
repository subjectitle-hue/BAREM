import SwiftUI

/// Android HomeTile.PRIMARY_COLUMNS — LazyVGrid iç içe scroll çökmesini önlemek için Row/Column grid.
enum BaremGrid {
    static let primaryColumns = 2
}

struct ChunkedTwoColumnGrid<Item: Identifiable, Content: View>: View {
    let items: [Item]
    let spacing: CGFloat
    @ViewBuilder let content: (Item) -> Content

    var body: some View {
        let columns = BaremGrid.primaryColumns
        VStack(spacing: spacing) {
            ForEach(Array(stride(from: 0, to: items.count, by: columns)), id: \.self) { start in
                HStack(spacing: spacing) {
                    let row = Array(items[start..<min(start + columns, items.count)])
                    ForEach(row) { item in
                        content(item)
                            .frame(maxWidth: .infinity)
                    }
                    if row.count < columns {
                        ForEach(0..<(columns - row.count), id: \.self) { _ in
                            Color.clear.frame(maxWidth: .infinity)
                        }
                    }
                }
            }
        }
    }
}

struct BaremHomePrimaryCard: View {
    let title: String
    let subtitle: String
    var tint: Color = .blue

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title).font(.headline)
            Text(subtitle).font(.caption).foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, minHeight: 90, alignment: .leading)
        .padding()
        .background(tint.opacity(0.12))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

struct BaremSelectionCard: View {
    let title: String
    var subtitle: String? = nil
    var selected: Bool = false

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title).font(.headline)
            if let subtitle {
                Text(subtitle).font(.caption).foregroundStyle(.secondary)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(selected ? Color.blue.opacity(0.2) : Color.gray.opacity(0.1))
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(selected ? Color.blue : Color.clear, lineWidth: 2)
        )
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

struct BaremStatPairRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
            Spacer()
            Text(value).fontWeight(.semibold)
        }
        .padding(.vertical, 4)
        Divider()
    }
}

func baremFormat(_ value: Double, digits: Int = 2) -> String {
    String(format: "%.\(digits)f", locale: Locale(identifier: "tr_TR"), value)
}
