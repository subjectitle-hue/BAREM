import Foundation

struct MKadroRow: Decodable {
    let hizmetSinifi: String
    let unvan: String
    let detay: String
    let derece: Int?
}

struct AFormOptions: Decodable {
    let medeniHal: [String]
    let yabanciDil: [String]
}

final class MemurCatalog {
    static let shared = MemurCatalog()

    private let kadroRows: [MKadroRow]
    private let aForm: AFormOptions

    private init() {
        kadroRows = Self.load("m_kadro", as: [MKadroRow].self, subdir: "memur")
        aForm = Self.load("a_form_options", as: AFormOptions.self, subdir: "memur")
    }

    private static let memurSinifOrder = [
        "GİH", "EÖHS", "EÖS", "SHS", "THS", "DHS", "AHS", "EHS", "YHS", "MİAH", "YARGI", "SP",
    ]

    private static let sinifLabels: [String: String] = [
        "GİH": "Genel İdare Hizmetleri",
        "AHS": "Avukatlık Hizmetleri",
        "SHS": "Sağlık Hizmetleri",
        "THS": "Teknik Hizmetler",
        "DHS": "Din Hizmetleri",
        "EHS": "Emniyet Hizmetleri",
        "EÖHS": "Eğitim Öğretim Hizmetleri",
        "EÖS": "Eğitim Öğretim Hizmetleri",
        "MİAH": "Mülki İdare Hizmetleri",
        "YHS": "Yardımcı Hizmetler",
        "YARGI": "Yargı Hizmetleri",
        "SP": "Sözleşmeli Personel",
    ]

    func hizmetSiniflari() -> [String] {
        let available = Set(kadroRows.map(\.hizmetSinifi))
        let ordered = Self.memurSinifOrder.filter { available.contains($0) }
        let rest = available.subtracting(ordered).sorted()
        return ordered + rest
    }

    func hizmetSinifiDisplay(_ code: String) -> String {
        if let label = Self.sinifLabels[code] { return "\(code) — \(label)" }
        return code
    }

    func kadroSinifiForLookup(_ tsSinifi: String) -> String {
        if tsSinifi == "SP" { return "GİH" }
        switch tsSinifi {
        case "EÖS": return "EÖHS"
        case "ÜE": return "EHS"
        case "EİHS": return "EÖHS"
        default: return tsSinifi
        }
    }

    func unvanlar(hizmetSinifi: String) -> [String] {
        Array(Set(kadroRows.filter { $0.hizmetSinifi == hizmetSinifi }.map(\.unvan))).sorted()
    }

    func searchUnvanlar(hizmetSinifi: String, query: String) -> [String] {
        let all = unvanlar(hizmetSinifi: hizmetSinifi)
        guard !query.isEmpty else { return all }
        return all.filter { $0.localizedCaseInsensitiveContains(query) }
    }

    func detaylar(hizmetSinifi: String, unvan: String) -> [String] {
        Array(Set(kadroRows.filter { $0.hizmetSinifi == hizmetSinifi && $0.unvan == unvan }.map(\.detay))).sorted()
    }

    func showDetayField(hizmetSinifi: String, unvan: String?) -> Bool {
        guard let unvan else { return false }
        return detaylar(hizmetSinifi: hizmetSinifi, unvan: unvan).contains { !$0.isEmpty }
    }

    func dereceler(hizmetSinifi: String, unvan: String, detay: String?) -> [Int] {
        kadroRows.filter {
            $0.hizmetSinifi == hizmetSinifi && $0.unvan == unvan &&
            (detay == nil || $0.detay == detay)
        }.compactMap(\.derece).sorted()
    }

    func medeniHalSecenekleri() -> [String] { aForm.medeniHal }
    func yabanciDilSecenekleri() -> [String] { aForm.yabanciDil }

    func hesapYillari(activeYear: Int) -> [Int] {
        EngineRepository.shared.periodResolver.availableYears(activeYear: activeYear)
    }

    private static func load<T: Decodable>(_ name: String, as type: T.Type, subdir: String) -> T {
        let url = Bundle.main.url(forResource: name, withExtension: "json", subdirectory: subdir)
            ?? Bundle.main.url(forResource: name, withExtension: "json")
        guard let url, let data = try? Data(contentsOf: url) else {
            fatalError("Missing memur resource: \(name).json")
        }
        return try! JSONDecoder().decode(T.self, from: data)
    }
}
