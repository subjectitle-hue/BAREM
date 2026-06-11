import Foundation
import SwiftUI

@MainActor
final class CalcSession: ObservableObject {
    static let shared = CalcSession()

    @Published var lastResult: SalaryResult?
    @Published var lastForm: MemurFormState?
    @Published var periodYear: Int = 2026
    @Published var yearlySeries: [YearlyCalcRow] = []
    @Published var yearlyLoading = false

    private init() {}

    var hasActiveSession: Bool { lastForm != nil && lastResult != nil }

    func save(form: MemurFormState, result: SalaryResult, year: Int) {
        lastForm = form
        lastResult = result
        periodYear = year
        yearlySeries = []
        CalcHistoryRepository.shared.saveFromSession(form: form, result: result)
    }

    func clearYearlySeries() { yearlySeries = [] }

    func refreshYearlySeriesIfNeeded() async {
        guard PremiumManager.shared.isPremium else { return }
        guard hasActiveSession, yearlySeries.isEmpty, !yearlyLoading else { return }
        guard let form = lastForm else { return }
        yearlyLoading = true
        let series = await Task.detached(priority: .userInitiated) {
            Self.computeYearlySeries(form: form)
        }.value
        yearlySeries = series
        yearlyLoading = false
    }

    func forceRefreshYearlySeries() async {
        guard PremiumManager.shared.isPremium, let form = lastForm else { return }
        yearlyLoading = true
        yearlySeries = []
        let series = await Task.detached(priority: .userInitiated) {
            Self.computeYearlySeries(form: form)
        }.value
        yearlySeries = series
        yearlyLoading = false
    }

    nonisolated private static func computeYearlySeries(form: MemurFormState) -> [YearlyCalcRow] {
        let repo = EngineRepository.shared
        let detay = form.kadroDetay ?? ""
        let kadro: MKadroFullRow?
        if let sinif = form.hizmetSinifi, let unvan = form.unvan, let derece = form.derece {
            kadro = repo.findKadroFull(hizmetSinifi: sinif, unvan: unvan, detay: detay, derece: derece)
        } else {
            kadro = nil
        }
        return YearlyCalcEngine().calculateSeries(form: form, kadro: kadro)
    }

    static func kadroSummaryLine(form: MemurFormState) -> String {
        var parts: [String] = []
        if let unvan = form.unvan { parts.append(unvan) }
        if let d = form.derece { parts.append("\(d). derece") }
        if let il = form.il {
            var loc = il
            if let ilce = form.ilce { loc += "/\(ilce)" }
            parts.append(loc)
        }
        return parts.joined(separator: " · ")
    }

    static func netSummaryLine(result: SalaryResult) -> String {
        "\(baremFormat(result.netAylik)) ₺ ort. net"
    }
}
