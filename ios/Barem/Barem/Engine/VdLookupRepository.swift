import Foundation

final class VdLookupRepository {
    static let shared = VdLookupRepository()

    private let data: VdIlIlceData

    private init() {
        let url = Bundle.main.url(forResource: "vd_il_ilce", withExtension: "json", subdirectory: "memur")
            ?? Bundle.main.url(forResource: "vd_il_ilce", withExtension: "json")
        guard let url, let raw = try? Data(contentsOf: url) else {
            fatalError("Missing memur/vd_il_ilce.json")
        }
        data = try! JSONDecoder().decode(VdIlIlceData.self, from: raw)
    }

    func iller() -> [String] { data.iller }

    func ilceler(il: String) -> [String] { data.ilcelerByIl[il] ?? [] }

    func bolgeselKod(il: String, ilce: String) -> String? {
        data.bolgeselKodByKey["\(il.trimmingCharacters(in: .whitespaces))-\(ilce.trimmingCharacters(in: .whitespaces))"] ?? nil
    }
}
