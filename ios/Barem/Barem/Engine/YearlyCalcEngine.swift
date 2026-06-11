import Foundation

/// Android YearlyCalcEngine — seçilen kadroya göre 1997–2026 yıllık net serisi.
final class YearlyCalcEngine {
    private let repo = EngineRepository.shared
    private let memurEngine = ExcelMemurEngine()

    func calculateSeries(form: MemurFormState, kadro: MKadroFullRow?) -> [YearlyCalcRow] {
        let activeYear = repo.activeYear
        let years = repo.periodResolver.availableYears(activeYear: activeYear)
        return years.compactMap { year in
            guard let pair = repo.semesterPairForYear(year) else { return nil }
            let result = memurEngine.calculateForPeriod(form: form, kadro: kadro, year: year, pair: pair)
            return YearlyCalcRow(
                year: year,
                monthlyNet: result.monthlyNet,
                yillikNet: result.yillikNet,
                yillikBrut: result.yillikBrut,
                firstHalfAvg: result.firstHalfAvg,
                secondHalfAvg: result.secondHalfAvg,
                goldPerGram: repo.goldRates(years: year...year).first?.yearlyValue,
                usdRate: repo.usdRates(years: year...year).first?.yearlyValue,
                fxOutput: result.fxOutput
            )
        }
    }
}
