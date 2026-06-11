import SwiftUI
#if canImport(Charts)
import Charts
#endif

private enum AnalyticsTab: String, CaseIterable, Identifiable {
    case tl, usd, eur, gold, inflation, deduction
    var id: String { rawValue }
    var title: String {
        switch self {
        case .tl: return "TL"
        case .usd: return "USD"
        case .eur: return "EUR"
        case .gold: return "Altın"
        case .inflation: return "Enflasyon"
        case .deduction: return "Kesinti"
        }
    }
}

struct AnalyticsChartsView: View {
    @ObservedObject private var session = CalcSession.shared
    @State private var selectedTab: AnalyticsTab
    private let repo = EngineRepository.shared

    init(initialTabIndex: Int = 0) {
        let tabs = AnalyticsTab.allCases
        let idx = min(max(0, initialTabIndex), tabs.count - 1)
        _selectedTab = State(initialValue: tabs[idx])
    }

    var body: some View {
        VStack(spacing: 0) {
            Picker("Sekme", selection: $selectedTab) {
                ForEach(AnalyticsTab.allCases) { tab in
                    Text(tab.title).tag(tab)
                }
            }
            .pickerStyle(.segmented)
            .padding()

            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    Text(subtitle).font(.subheadline).foregroundStyle(.secondary)
                    chartContent
                }
                .padding()
            }
        }
        .navigationTitle("Maaş grafikleri")
        .task { await session.refreshYearlySeriesIfNeeded() }
    }

    private var subtitle: String {
        switch selectedTab {
        case .tl: return "Yıllık ortalama net maaş (TL)"
        case .usd: return "Yıllık ortalama net / dolar kuru"
        case .eur: return "Yıllık ortalama net / euro kuru"
        case .gold: return "Yıllık ortalama net / çeyrek altın"
        case .inflation: return "Enflasyon (İ) vs maaş artışı (%)"
        case .deduction: return "Brüt üzerinden net / prim / GV / DV oranları"
        }
    }

    @ViewBuilder
    private var chartContent: some View {
        let rows = dataRows
        if rows.isEmpty {
            Text("Bu grafik için önce Memur sihirbazından hesap yapın veya istatistik verisini kullanın.")
                .foregroundStyle(.secondary)
        } else {
            #if canImport(Charts)
            if #available(iOS 16.0, *) {
                Chart(rows.sorted(by: { $0.year < $1.year })) { row in
                    LineMark(x: .value("Yıl", row.year), y: .value("Değer", row.yearlyValue))
                        .foregroundStyle(.blue)
                }
                .frame(height: 260)
            } else {
                fallbackTable(rows)
            }
            #else
            fallbackTable(rows)
            #endif
        }
    }

    private var dataRows: [YearValueRow] {
        switch selectedTab {
        case .tl:
            if !session.yearlySeries.isEmpty {
                return session.yearlySeries.map { row in
                    let avg = row.monthlyNet.isEmpty
                        ? (row.firstHalfAvg + row.secondHalfAvg) / 2
                        : row.monthlyNet.reduce(0, +) / Double(row.monthlyNet.count)
                    return YearValueRow(year: row.year, yearlyValue: avg)
                }
            }
            return repo.netAsgari()
        case .usd:
            if !session.yearlySeries.isEmpty {
                return session.yearlySeries.compactMap { row in
                    row.fxOutput?.yearlyAvgUsd.map { YearValueRow(year: row.year, yearlyValue: $0) }
                }
            }
            return repo.usdRates()
        case .eur:
            if !session.yearlySeries.isEmpty {
                return session.yearlySeries.compactMap { row in
                    row.fxOutput?.yearlyAvgEur.map { YearValueRow(year: row.year, yearlyValue: $0) }
                }
            }
            return []
        case .gold:
            if !session.yearlySeries.isEmpty {
                return session.yearlySeries.compactMap { row in
                    row.fxOutput?.yearlyAvgGoldQuarter.map { YearValueRow(year: row.year, yearlyValue: $0) }
                }
            }
            return repo.goldRates()
        case .inflation:
            return repo.inflationSeries()
        case .deduction:
            return memurRaiseFromSeries().isEmpty
                ? repo.asgariRaisePct(from: repo.netAsgari())
                : memurRaiseFromSeries()
        }
    }

    private func memurRaiseFromSeries() -> [YearValueRow] {
        let nets = session.yearlySeries.sorted { $0.year < $1.year }.map { row -> YearValueRow in
            let avg = row.monthlyNet.isEmpty
                ? (row.firstHalfAvg + row.secondHalfAvg) / 2
                : row.monthlyNet.reduce(0, +) / Double(row.monthlyNet.count)
            return YearValueRow(year: row.year, yearlyValue: avg)
        }
        guard nets.count > 1 else { return [] }
        return zip(nets, nets.dropFirst()).map { prev, next in
            let pct = prev.yearlyValue > 0
                ? (next.yearlyValue - prev.yearlyValue) / prev.yearlyValue * 100
                : 0
            return YearValueRow(year: next.year, yearlyValue: pct)
        }
    }

    private func fallbackTable(_ rows: [YearValueRow]) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            ForEach(rows.sorted(by: { $0.year > $1.year })) { row in
                BaremStatPairRow(label: String(row.year), value: baremFormat(row.yearlyValue))
            }
        }
    }
}
