import Foundation

final class CalcHistoryRepository {
    static let shared = CalcHistoryRepository()
    static let maxEntries = 30

    private let fileURL: URL

    private init() {
        let dir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
        fileURL = dir.appendingPathComponent("calc_history.json")
    }

    func list() -> [CalcHistoryRecord] {
        guard let data = try? Data(contentsOf: fileURL) else { return [] }
        return (try? JSONDecoder().decode([CalcHistoryRecord].self, from: data)) ?? []
    }

    func delete(id: String) {
        save(list().filter { $0.id != id })
    }

    func saveFromSession(form: MemurFormState, result: SalaryResult) {
        let record = CalcHistoryRecord(
            id: UUID().uuidString,
            savedAtEpochMs: Int64(Date().timeIntervalSince1970 * 1000),
            form: form,
            kadroSummary: CalcSession.kadroSummaryLine(form: form),
            netSummary: CalcSession.netSummaryLine(result: result),
            periodLabel: result.periodLabel
        )
        var next = [record] + list()
        var seen = Set<String>()
        next = next.filter { seen.insert(dedupeKey($0.form)).inserted }
        save(Array(next.prefix(Self.maxEntries)))
    }

    private func dedupeKey(_ form: MemurFormState) -> String {
        [
            form.hizmetSinifi, form.unvan, form.kadroDetay,
            form.hesapYili.map(String.init), form.derece.map(String.init),
            form.medeniHal, form.kademe.map(String.init), form.kidemYili.map(String.init),
            form.il, form.ilce, form.topluSozlesme,
            String(form.cocukUst6), String(form.cocukAlt6),
        ].map { $0 ?? "" }.joined(separator: "|")
    }

    private func save(_ records: [CalcHistoryRecord]) {
        guard let data = try? JSONEncoder().encode(records) else { return }
        try? data.write(to: fileURL, options: .atomic)
    }
}
