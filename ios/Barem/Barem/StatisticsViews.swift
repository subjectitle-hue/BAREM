import SwiftUI

private let statisticsYearRange = 2000...2026

private enum SalaryStatTab: String, CaseIterable, Identifiable {
    case tl, usd, eur, gold, asgari
    var id: String { rawValue }
    var title: String {
        switch self {
        case .tl: return "TL"
        case .usd: return "USD"
        case .eur: return "EUR"
        case .gold: return "Altın"
        case .asgari: return "Asgari"
        }
    }
}

private struct StatisticsValues {
    let tl: Double?
    let usd: Double?
    let eur: Double?
    let goldQuarter: Double?
    let asgariRatio: Double?
}

struct GeneralStatisticsHubView: View {
    var body: some View {
        NavigationStack {
            List {
                Section {
                    Text("Maaş, kur ve mevzuat istatistikleri. İstenirse aylık değerlere çevrilebilir.")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .listRowBackground(Color.clear)
                }
                ForEach(StatKind.allCases) { kind in
                    NavigationLink {
                        destination(for: kind)
                    } label: {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(kind.title).font(.headline)
                            Text(kind.subtitle).font(.caption).foregroundStyle(.secondary)
                        }
                        .padding(.vertical, 4)
                    }
                }
            }
            .navigationTitle("Genel İstatistik")
        }
    }

    @ViewBuilder
    private func destination(for kind: StatKind) -> some View {
        if kind == .salaryTable {
            SalaryTableView()
        } else {
            StatDetailView(kind: kind)
        }
    }
}

struct StatDetailView: View {
    let kind: StatKind
    @State private var monthly = false
    @ObservedObject private var session = CalcSession.shared
    private let repo = EngineRepository.shared

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            if kind.showsChart {
                NavigationLink {
                    AnalyticsChartsView(initialTabIndex: kind.chartTabIndex)
                } label: {
                    Text("Grafikle göster")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
            }

            if kind != .prim && kind != .gv {
                Button(monthly ? "Yıllık göster" : "Aylığa çevir") { monthly.toggle() }
                    .buttonStyle(.bordered)
                    .frame(maxWidth: .infinity)
            }

            content
        }
        .padding()
        .navigationTitle(kind.title)
        .task {
            if kind == .memurRaise || kind == .dollar || kind == .gold {
                await session.refreshYearlySeriesIfNeeded()
            }
        }
    }

    @ViewBuilder
    private var content: some View {
        switch kind {
        case .dollar:
            valueList(dollarRows(), suffix: "$", digits: 2)
        case .gold:
            valueList(goldRows(), suffix: "çeyrek", digits: 2)
        case .asgari:
            valueList(repo.netAsgari(), suffix: "₺", digits: 2)
        case .asgariRaise:
            valueList(repo.asgariRaisePct(from: repo.netAsgari()), suffix: "%", digits: 1)
        case .memurRaise:
            if session.yearlyLoading {
                ProgressView("Premium veriler hazırlanıyor…")
            } else {
                let rows = memurRaiseRows()
                if rows.isEmpty {
                    Text(session.hasActiveSession
                         ? "Tam seri için premium gerekir."
                         : "Bu ekran için önce Memur sihirbazından hesap yapın.")
                        .foregroundStyle(.secondary)
                } else {
                    valueList(rows, suffix: "%", digits: 1)
                }
            }
        case .prim:
            ForEach(repo.primRates(), id: \.0) { label, rate in
                BaremStatPairRow(label: "SGK \(label)", value: String(format: "%.2f%%", rate * 100))
            }
        case .gv:
            let labels = repo.gvBracketLabels()
            let values = repo.gvBracketsBn()
            ForEach(labels.indices, id: \.self) { i in
                BaremStatPairRow(
                    label: labels[i],
                    value: values.indices.contains(i) ? (values[i].map { baremFormat($0, digits: 0) } ?? "—") : "—"
                )
            }
        case .salaryTable:
            EmptyView()
        }
    }

    private func dollarRows() -> [YearValueRow] {
        if !session.yearlySeries.isEmpty {
            return session.yearlySeries.compactMap { row in
                row.fxOutput?.yearlyAvgUsd.map { YearValueRow(year: row.year, yearlyValue: $0) }
            }
        }
        return repo.usdRates()
    }

    private func goldRows() -> [YearValueRow] {
        if !session.yearlySeries.isEmpty {
            return session.yearlySeries.compactMap { row in
                row.fxOutput?.yearlyAvgGoldQuarter.map { YearValueRow(year: row.year, yearlyValue: $0) }
            }
        }
        return repo.goldRates()
    }

    private func memurRaiseRows() -> [YearValueRow] {
        let nets = session.yearlySeries
            .filter { statisticsYearRange.contains($0.year) }
            .sorted { $0.year < $1.year }
            .map { row -> YearValueRow in
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

    @ViewBuilder
    private func valueList(_ rows: [YearValueRow], suffix: String, digits: Int) -> some View {
        if rows.isEmpty {
            Text("Veri bulunamadı.").foregroundStyle(.secondary)
        } else {
            ForEach(rows.sorted(by: { $0.year > $1.year })) { row in
                let v = monthly ? row.monthlyValue : row.yearlyValue
                BaremStatPairRow(label: String(row.year), value: "\(baremFormat(v, digits: digits)) \(suffix)")
            }
        }
    }
}

struct SalaryTableView: View {
    @ObservedObject private var session = CalcSession.shared
    @State private var selectedTab: SalaryStatTab = .tl
    @State private var expandedYear: Int?

    var body: some View {
        PremiumGate {
            if let result = session.lastResult {
                VStack(alignment: .leading, spacing: 8) {
                    Text("\(result.periodLabel) net (güncel)")
                        .font(.subheadline)
                    Text("\(baremFormat(result.netAylik)) ₺")
                        .font(.title2.bold())
                }
            } else {
                Text("Önizleme — hesap yapınca güncel yıl gösterilir.")
                    .foregroundStyle(.secondary)
            }
        } content: {
            salaryTableContent
        }
        .padding()
        .navigationTitle("2000–2026 Maaş İstatistiği")
        .task {
            await session.refreshYearlySeriesIfNeeded()
        }
    }

    @ViewBuilder
    private var salaryTableContent: some View {
        if !session.hasActiveSession {
            Text("Bu ekran için önce Memur sihirbazından hesap yapın.")
                .foregroundStyle(.secondary)
        } else if session.yearlyLoading {
            ProgressView("Premium veriler hazırlanıyor…")
        } else if session.yearlySeries.isEmpty {
            Text("Yıllık seri hesaplanamadı.")
                .foregroundStyle(.secondary)
        } else {
            NavigationLink {
                AnalyticsChartsView(initialTabIndex: 0)
            } label: {
                Text("Grafikle göster")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)

            Text("2000–2026 yıllık ortalama net; yıla dokunarak aylık değerleri açıp kapatabilirsiniz.")
                .font(.subheadline)
                .foregroundStyle(.secondary)

            Picker("Sekme", selection: $selectedTab) {
                ForEach(SalaryStatTab.allCases) { tab in
                    Text(tab.title).tag(tab)
                }
            }
            .pickerStyle(.segmented)

            VStack(spacing: 0) {
                HStack {
                    Text("Dönem").font(.caption.bold()).frame(width: 72, alignment: .leading)
                    Text(selectedTab.title).font(.caption.bold())
                    Spacer()
                }
                .padding(.vertical, 8)
                Divider()

                ForEach(filteredRows) { row in
                    let yearly = statisticsYearlyValues(row)
                    let isExpanded = expandedYear == row.year
                    Button {
                        expandedYear = isExpanded ? nil : row.year
                    } label: {
                        HStack {
                            HStack(spacing: 4) {
                                Image(systemName: isExpanded ? "chevron.down" : "chevron.right")
                                    .font(.caption2)
                                    .foregroundStyle(.secondary)
                                Text(String(row.year)).fontWeight(.semibold)
                            }
                            .frame(width: 72, alignment: .leading)
                            Text(formatStatisticsValue(yearly, tab: selectedTab))
                            Spacer()
                        }
                        .padding(.vertical, 8)
                    }
                    .buttonStyle(.plain)
                    Divider()
                    if isExpanded {
                        ForEach(Array(SalaryResult.monthLabels.enumerated()), id: \.offset) { index, month in
                            if index < row.monthlyNet.count {
                                let net = row.monthlyNet[index]
                                let monthly = StatisticsValues(
                                    tl: net,
                                    usd: row.fxOutput?.monthlyUsd[safe: index],
                                    eur: row.fxOutput?.monthlyEur[safe: index],
                                    goldQuarter: row.fxOutput?.monthlyGoldQuarter[safe: index],
                                    asgariRatio: asgariRatio(net: net, year: row.year, monthIndex: index)
                                )
                                HStack {
                                    Text(month)
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                        .frame(width: 72, alignment: .leading)
                                        .padding(.leading, 16)
                                    Text(formatStatisticsValue(monthly, tab: selectedTab))
                                        .font(.caption)
                                    Spacer()
                                }
                                .padding(.vertical, 4)
                            }
                        }
                    }
                }
            }
            .background(Color.gray.opacity(0.08))
            .clipShape(RoundedRectangle(cornerRadius: 12))

            Text("Seçtiğiniz kadroya göre hesaplanmış yıllık seri.")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
    }

    private var filteredRows: [YearlyCalcRow] {
        session.yearlySeries
            .filter { statisticsYearRange.contains($0.year) }
            .sorted { $0.year > $1.year }
    }

    private func statisticsYearlyValues(_ row: YearlyCalcRow) -> StatisticsValues {
        let tl = row.monthlyNet.isEmpty
            ? (row.firstHalfAvg + row.secondHalfAvg) / 2
            : row.monthlyNet.reduce(0, +) / Double(row.monthlyNet.count)
        let ratios = row.monthlyNet.enumerated().compactMap { index, net in
            asgariRatio(net: net, year: row.year, monthIndex: index)
        }
        return StatisticsValues(
            tl: tl,
            usd: row.fxOutput?.yearlyAvgUsd,
            eur: row.fxOutput?.yearlyAvgEur,
            goldQuarter: row.fxOutput?.yearlyAvgGoldQuarter,
            asgariRatio: ratios.isEmpty ? nil : ratios.reduce(0, +) / Double(ratios.count)
        )
    }

    private func asgariRatio(net: Double, year: Int, monthIndex: Int) -> Double? {
        guard let asgari = EngineRepository.shared.netAsgariUcret(year: year, secondHalf: monthIndex >= 6),
              asgari > 0 else { return nil }
        return net / asgari
    }

    private func formatStatisticsValue(_ values: StatisticsValues, tab: SalaryStatTab) -> String {
        switch tab {
        case .tl: return values.tl.map { "\(baremFormat($0)) ₺" } ?? "—"
        case .usd: return values.usd.map { "$\(baremFormat($0))" } ?? "—"
        case .eur: return values.eur.map { "\(baremFormat($0)) €" } ?? "—"
        case .gold: return values.goldQuarter.map { "\(baremFormat($0)) çeyrek" } ?? "—"
        case .asgari: return values.asgariRatio.map { "\(baremFormat($0))×" } ?? "—"
        }
    }
}

struct HistoryView: View {
    @State private var records: [CalcHistoryRecord] = []

    var body: some View {
        NavigationStack {
            Group {
                if records.isEmpty {
                    Text("Kayıtlı hesaplar cihazınızda saklanır. Açınca seçim yeniden hesaplanır.")
                        .foregroundStyle(.secondary)
                        .padding()
                } else {
                    List(records) { record in
                        NavigationLink {
                            MemurWizardView(restoreForm: record.form)
                        } label: {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(record.kadroSummary).font(.headline)
                                Text(record.netSummary).font(.subheadline).foregroundStyle(.secondary)
                                Text(record.periodLabel).font(.caption).foregroundStyle(.tertiary)
                            }
                        }
                        .swipeActions {
                            Button(role: .destructive) {
                                CalcHistoryRepository.shared.delete(id: record.id)
                                reload()
                            } label: {
                                Text("Sil")
                            }
                        }
                    }
                }
            }
            .navigationTitle("Geçmiş")
            .onAppear { reload() }
        }
    }

    private func reload() {
        records = CalcHistoryRepository.shared.list()
    }
}

struct YearlyBordroView: View {
    let result: SalaryResult
    @ObservedObject private var session = CalcSession.shared
    @ObservedObject private var premium = PremiumManager.shared

    var body: some View {
        List {
            Section("Özet") {
                BaremStatPairRow(label: "Dönem", value: result.periodLabel)
                BaremStatPairRow(label: "Yıllık ortalama brüt", value: "\(baremFormat(result.yillikBrut / 12)) ₺")
                BaremStatPairRow(label: "Yıllık ortalama net", value: "\(baremFormat(result.netAylik)) ₺")
                BaremStatPairRow(label: "1. dönem ortalama", value: "\(baremFormat(result.firstHalfAvg)) ₺")
                BaremStatPairRow(label: "2. dönem ortalama", value: "\(baremFormat(result.secondHalfAvg)) ₺")
            }
            Section("Aylık net") {
                ForEach(Array(SalaryResult.monthLabels.enumerated()), id: \.offset) { i, label in
                    if i < result.monthlyNet.count {
                        BaremStatPairRow(label: label, value: "\(baremFormat(result.monthlyNet[i])) ₺")
                    }
                }
            }
            if premium.isPremium {
                Section("Yıllık net geçmişi (premium)") {
                    if session.yearlyLoading {
                        Text("Premium veriler hazırlanıyor…")
                    } else {
                        ForEach(session.yearlySeries.sorted(by: { $0.year > $1.year })) { row in
                            HStack {
                                Text(String(row.year))
                                Spacer()
                                VStack(alignment: .trailing) {
                                    Text("\(baremFormat(row.yillikNet)) ₺").fontWeight(.semibold)
                                    Text("ort. \(baremFormat(row.firstHalfAvg)) ₺")
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                }
                            }
                        }
                    }
                }
            } else {
                Section {
                    PremiumUpsellView()
                }
            }
        }
        .navigationTitle("BORDRO (Yıllık)")
        .task { await session.refreshYearlySeriesIfNeeded() }
    }
}

private extension Array {
    subscript(safe index: Int) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}
