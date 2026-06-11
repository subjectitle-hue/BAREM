import Foundation

struct HEngineFullRoot: Decodable {
    let meta: HEngineMeta
    let coefficients: SemesterCoeffs
    let gvMeta: GvMetaFull
    let fxByYear: FxByYear
}

struct HEngineMeta: Decodable {
    let activePeriod: String
    let activeYear: Int
    let deductionMode: String?
}

struct SemesterCoeffs: Decodable {
    let ocak: CoeffSet
    let temmuz: CoeffSet
}

struct CoeffSet: Decodable {
    let column: String
    let memurK: Double
    let tabanK: Double
    let yanK: Double
    let enYuksek: Double
}

struct GvMetaFull: Decodable {
    let primOran5434: Double
    let primOran5510: Double
    let gvBracketsBn: [Double?]
    let sgkTipi: String
    let deductionMode: String?
}

struct FxByYear: Decodable {
    let goldPerGram: [String: Double]
    let usdRate: [String: Double]
}

struct IndexTableRoot: Decodable {
    let entries: [IndexEntry]
}

struct IndexTableFullRoot: Decodable {
    let headerMeta: [HeaderMeta]
    let entries: [IndexEntry]
}

struct HeaderMeta: Decodable {
    let key: String
    let year: Int?
}

struct IndexEntry: Decodable {
    let label: String
    let values: [String: IndexValue]
}

enum IndexValue: Decodable {
    case number(Double)
    case text(String)

    init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        if let n = try? container.decode(Double.self) {
            self = .number(n)
        } else {
            self = .text(try container.decode(String.self))
        }
    }

    var doubleValue: Double? {
        switch self {
        case .number(let n): return n
        case .text(let s): return Double(s.replacingOccurrences(of: ",", with: "."))
        }
    }
}

struct YearValueRow: Identifiable {
    let year: Int
    let yearlyValue: Double
    var monthlyValue: Double { yearlyValue / 12.0 }
    var id: Int { year }
}

struct PeriodMapRoot: Decodable {
    let periods: [PeriodMapEntry]
}

struct PeriodMapEntry: Decodable {
    let col: String
    let year: Int?
    let semester: String?
    let memurK: Double?
    let tabanK: Double?
    let yanK: Double?
    let enYuksek: Double?

    func toCoeffSet() -> CoeffSet {
        CoeffSet(
            column: col,
            memurK: memurK ?? 0,
            tabanK: tabanK ?? 0,
            yanK: yanK ?? 0,
            enYuksek: enYuksek ?? 0
        )
    }
}

struct SemesterColumnPair {
    let ocak: String
    let temmuz: String
    let ocakCoeffs: CoeffSet
    let temmuzCoeffs: CoeffSet
}

struct GvBrackets: Decodable {
    let dilimLimits: [Double]
    let dilimBaseTax: [Double]
    let dilimRates: [Double]
    let damgaRate: Double
    let damgaMuafiyetBm: Double?
    let agiMonthly: Double
    let deductionMode: String?
    let crossColumnThresholds: CrossColumnThresholds?
}

struct CrossColumnThresholds: Decodable {
    let gvFirstHalf: [Double]
    let gvSecondHalf: [Double]
    let dvMuafOcak: Double
    let dvMuafTem: Double
}

struct MHLookupRoot: Decodable {
    let rows: [MHLookupRow]
}

struct MHLookupRow: Decodable {
    let key: String
    let unvan: String
    let detay: String
    let derece: Int
    let ekCode: String?
    let tazminatCode: String?
    let ilaveTazPrimeDahilCode: String?
    let ilaveTazPrimeHaricCode: String?
    let yanOdemeCode: String?
    let ekOdemeCode: String?
    let bolgeselCode: String?
    let payCodes: [String: String]?
    let tsPrimeKod: String?
}

struct MKadroFullRow: Decodable {
    let hizmetSinifi: String
    let barem: Int?
    let unvan: String
    let detay: String
    let derece: Int?
    let taban: Double
    let kidem: Double
    let ekGosterge: String
}

struct MemurFormState: Codable, Equatable {
    var hizmetSinifi: String?
    var hesapYili: Int?
    var unvan: String?
    var kadroDetay: String?
    var derece: Int?
    var kademe: Int?
    var kidemYili: Int?
    var medeniHal: String?
    var cocukUst6: Int = 0
    var cocukAlt6: Int = 0
    var topluSozlesme: String = "YOK"
    var yabanciDil: String?
    var il: String?
    var ilce: String?
    var bolgeselKod: String?
}

struct FxOutput {
    let monthlyUsd: [Double]
    let monthlyEur: [Double]
    let monthlyGoldQuarter: [Double]
    let yearlyAvgUsd: Double
    let yearlyAvgEur: Double
    let yearlyAvgGoldQuarter: Double
}

struct FxSemesterRatesRoot: Decodable {
    let byYear: [String: FxSemesterRates]
}

struct FxSemesterRates: Decodable {
    let usdOcak: Double?
    let usdTemmuz: Double?
    let eurOcak: Double?
    let eurTemmuz: Double?
    let goldQuarter: Double?
}

struct YearlyCalcRow: Identifiable {
    let year: Int
    let monthlyNet: [Double]
    let yillikNet: Double
    let yillikBrut: Double
    let firstHalfAvg: Double
    let secondHalfAvg: Double
    let goldPerGram: Double?
    let usdRate: Double?
    let fxOutput: FxOutput?
    var id: Int { year }
}

struct CalcHistoryRecord: Identifiable, Codable {
    let id: String
    let savedAtEpochMs: Int64
    let form: MemurFormState
    let kadroSummary: String
    let netSummary: String
    let periodLabel: String
}

struct SalaryResult {
    let brutAylik: Double
    let kesintiler: Double
    let netAylik: Double
    let cocukYardimi: Double
    let yillikBrut: Double
    let yillikNet: Double
    let firstHalfAvg: Double
    let secondHalfAvg: Double
    let altinGramKarsilik: Double
    let dolarKarsilik: Double
    let periodLabel: String
    let monthlyNet: [Double]
    let fxOutput: FxOutput?
    static let monthLabels = [
        "OCAK", "ŞUBAT", "MART", "NİSAN", "MAYIS", "HAZİRAN",
        "TEMMUZ", "AĞUSTOS", "EYLÜL", "EKİM", "KASIM", "ARALIK",
    ]
}

struct VdIlIlceData: Decodable {
    let iller: [String]
    let ilcelerByIl: [String: [String]]
    let bolgeselKodByKey: [String: String?]
}

enum ResultCurrency: CaseIterable, Hashable {
    case tl, usd, eur, gold

    var label: String {
        switch self {
        case .tl: return "TL"
        case .usd: return "USD"
        case .eur: return "EUR"
        case .gold: return "Altın"
        }
    }
}
