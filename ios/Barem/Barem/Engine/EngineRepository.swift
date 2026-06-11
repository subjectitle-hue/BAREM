import Foundation

final class EngineRepository {
    static let shared = EngineRepository()

    private let engineFull: HEngineFullRoot
    private let indexEntries: [IndexEntry]
    private let indexTableEntries: [IndexEntry]
    private let vTable: [String: [String: Double]]
    private let mKadroFull: [MKadroFullRow]
    private let mLookupByKey: [String: MHLookupRow]
    private let tsIndicators: [String: [String: Double]]
    private let gvBrackets: GvBrackets
    private let fxSemesterRates: [String: FxSemesterRates]
    let periodResolver: PeriodColumnResolver

    private init() {
        engineFull = Self.load("h_engine_full", as: HEngineFullRoot.self)
        indexTableEntries = Self.load("index_table", as: IndexTableRoot.self).entries
        let indexFull: IndexTableFullRoot = Self.load("index_table_full", as: IndexTableFullRoot.self)
        indexEntries = indexFull.entries
        vTable = Self.load("v_derece_kademe", as: [String: [String: Double]].self)
        mKadroFull = Self.load("m_kadro_full", as: [MKadroFullRow].self)
        let mLookupRoot: MHLookupRoot = Self.load("m_h_lookup", as: MHLookupRoot.self)
        mLookupByKey = Dictionary(uniqueKeysWithValues: mLookupRoot.rows.map { ($0.key, $0) })
        tsIndicators = Self.load("ts_indicator_by_kod", as: [String: [String: Double]].self)
        gvBrackets = Self.load("gv_brackets", as: GvBrackets.self)
        let periodRoot: PeriodMapRoot = Self.load("h_period_map", as: PeriodMapRoot.self)
        periodResolver = PeriodColumnResolver(periods: periodRoot.periods)
        fxSemesterRates = Self.load("fx_semester_rates", as: FxSemesterRatesRoot.self).byYear
    }

    func fxRatesForYear(_ year: Int) -> FxSemesterRates? {
        fxSemesterRates[String(year)]
    }

    var activeYear: Int { engineFull.meta.activeYear }
    var gvMeta: GvMetaFull { engineFull.gvMeta }
    func brackets() -> GvBrackets { gvBrackets }

    // MARK: - EngineStatisticsBuilder parity

    private func statYears() -> ClosedRange<Int> { 2000...min(2026, activeYear) }

    func usdRates(years: ClosedRange<Int>? = nil) -> [YearValueRow] {
        let range = years ?? statYears()
        return range.compactMap { year in
            guard let rate = engineFull.fxByYear.usdRate[String(year)] else { return nil }
            return YearValueRow(year: year, yearlyValue: rate)
        }
    }

    func goldRates(years: ClosedRange<Int>? = nil) -> [YearValueRow] {
        let range = years ?? statYears()
        return range.compactMap { year in
            guard let rate = engineFull.fxByYear.goldPerGram[String(year)] else { return nil }
            return YearValueRow(year: year, yearlyValue: rate)
        }
    }

    func netAsgari(years: ClosedRange<Int>? = nil) -> [YearValueRow] {
        let range = years ?? statYears()
        return range.compactMap { year in
            guard let ocak = netAsgariUcret(year: year, secondHalf: false) else { return nil }
            let temmuz = netAsgariUcret(year: year, secondHalf: true) ?? ocak
            let avg = (ocak + temmuz) / 2.0
            return YearValueRow(year: year, yearlyValue: avg)
        }
    }

    func netAsgariUcret(year: Int, secondHalf: Bool = false) -> Double? {
        guard let pair = periodResolver.semesterColumnsForYear(year) else { return nil }
        let coeffs = secondHalf ? pair.temmuzCoeffs : pair.ocakCoeffs
        let period = "\(year)-\(coeffs.memurK)"
        let value = indexLookup(label: "Net Asgari Ücret", period: period)
        return value > 0 ? value : nil
    }

    func asgariRaisePct(from rows: [YearValueRow]) -> [YearValueRow] {
        guard rows.count > 1 else { return [] }
        return zip(rows, rows.dropFirst()).map { prev, next in
            let pct = prev.yearlyValue > 0
                ? (next.yearlyValue - prev.yearlyValue) / prev.yearlyValue * 100
                : 0
            return YearValueRow(year: next.year, yearlyValue: pct)
        }
    }

    func inflationRate(year: Int) -> Double? {
        guard let pair = periodResolver.semesterColumnsForYear(year) else { return nil }
        let period = "\(year)-\(pair.ocakCoeffs.memurK)"
        let v = indexLookup(label: "ENFLASYON", period: period)
        return v > 0 ? v : nil
    }

    func inflationSeries() -> [YearValueRow] {
        statYears().compactMap { year in
            inflationRate(year: year).map { YearValueRow(year: year, yearlyValue: $0) }
        }
    }

    func primRates() -> [(String, Double)] {
        [
            ("5434", gvMeta.primOran5434),
            ("5510", gvMeta.primOran5510),
        ]
    }

    func gvBracketLabels() -> [String] {
        ["1. dilim", "2. dilim", "3. dilim", "4. dilim", "5. dilim"]
    }

    func gvBracketsBn() -> [Double?] { gvMeta.gvBracketsBn }

    // MARK: - Excel engine lookups

    func indexLookup(label: String, period: String) -> Double {
        let entry = indexTableEntries.first { $0.label.caseInsensitiveCompare(label) == .orderedSame }
            ?? indexEntries.first { $0.label.localizedCaseInsensitiveContains(label) }
        guard let entry, let raw = entry.values[period]?.doubleValue else { return 0 }
        return raw
    }

    func vLookupDereceKademe(derece: Int, kademe: Int, year: String) -> Double {
        vLookupTable(key: "\(derece)/\(kademe)", year: year)
    }

    func vLookupTable(key: String, year: String) -> Double {
        vTable[key]?[year] ?? 0
    }

    func ekGostergePoints(ekCode: String?, derece: Int, year: String) -> Double {
        guard let ekCode, !ekCode.isEmpty else { return 0 }
        return vLookupTable(key: "\(ekCode)-\(derece)", year: year)
    }

    func vLookupPayIndicator(code: String?, derece: Int, year: String) -> Double {
        guard let code, !code.isEmpty else { return 0 }
        let keyed = vLookupTable(key: "\(code)-\(derece)", year: year)
        if keyed > 0 { return keyed }
        return vLookupTable(key: code, year: year)
    }

    func tsPrimeIndicator(kod: String?, year: String) -> Double {
        guard let kod, !kod.isEmpty else { return 0 }
        return tsIndicators[kod]?[year] ?? 0
    }

    func mLookupForKadro(unvan: String?, detay: String?, derece: Int?) -> MHLookupRow? {
        guard let unvan, let derece else { return nil }
        let key = "\(unvan.trimmingCharacters(in: .whitespaces))-\(detay?.trimmingCharacters(in: .whitespaces) ?? "")-\(derece)"
        return mLookupByKey[key]
    }

    func tsPrimeIndicatorForKadro(unvan: String?, detay: String?, derece: Int?, year: String) -> Double {
        guard let row = mLookupForKadro(unvan: unvan, detay: detay, derece: derece),
              let kod = row.tsPrimeKod else { return 0 }
        return tsPrimeIndicator(kod: kod, year: year)
    }

    func ilaveOdemeGostergesi(year: String, hasTazminatPath: Bool) -> Double {
        guard hasTazminatPath else { return 0 }
        for key in ["İlave Ödeme", "Ilave Odeme", "İlave Ödeme "] {
            let v = vLookupTable(key: key, year: year)
            if v > 0 { return v }
        }
        return 0
    }

    func findKadroFull(hizmetSinifi: String, unvan: String, detay: String, derece: Int?) -> MKadroFullRow? {
        let lookupSinif = MemurCatalog.shared.kadroSinifiForLookup(hizmetSinifi)
        return mKadroFull.first {
            $0.hizmetSinifi == lookupSinif &&
            $0.unvan == unvan &&
            $0.detay == detay &&
            $0.derece == derece
        }
    }

    func goldRate(period: String) -> Double {
        let year = Int(period.prefix(while: { $0.isNumber })) ?? activeYear
        return engineFull.fxByYear.goldPerGram[String(year)] ?? 0
    }

    func usdRate(period: String) -> Double {
        let year = Int(period.prefix(while: { $0.isNumber })) ?? activeYear
        return engineFull.fxByYear.usdRate[String(year)] ?? 0
    }

    func activeSemesterPair() -> SemesterColumnPair {
        periodResolver.activeYearPair(activeYear: activeYear)
    }

    func semesterPairForYear(_ year: Int) -> SemesterColumnPair? {
        periodResolver.semesterColumnsForYear(year)
    }

    private static func load<T: Decodable>(_ name: String, as type: T.Type) -> T {
        let url = Bundle.main.url(forResource: name, withExtension: "json", subdirectory: "engine")
            ?? Bundle.main.url(forResource: name, withExtension: "json")
        guard let url, let data = try? Data(contentsOf: url) else {
            fatalError("Missing engine resource: \(name).json")
        }
        return try! JSONDecoder().decode(T.self, from: data)
    }
}
