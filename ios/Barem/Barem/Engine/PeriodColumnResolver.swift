import Foundation

final class PeriodColumnResolver {
    private let periods: [PeriodMapEntry]

    init(periods: [PeriodMapEntry]) {
        self.periods = periods
    }

    func semesterColumnsForYear(_ year: Int) -> SemesterColumnPair? {
        guard let veriIdx = periods.firstIndex(where: {
            $0.year == year && ($0.semester?.uppercased() == "VERİ" || $0.semester?.uppercased() == "VERI")
        }) else { return nil }
        let tail = periods.dropFirst(veriIdx + 1)
        guard let ocak = tail.first(where: { $0.semester?.uppercased() == "OCAK" }),
              let temmuz = tail.first(where: { $0.semester?.uppercased() == "TEMMUZ" }) else { return nil }
        return SemesterColumnPair(
            ocak: ocak.col,
            temmuz: temmuz.col,
            ocakCoeffs: ocak.toCoeffSet(),
            temmuzCoeffs: temmuz.toCoeffSet()
        )
    }

    func activeYearPair(activeYear: Int = 2026) -> SemesterColumnPair {
        semesterColumnsForYear(activeYear) ?? SemesterColumnPair(
            ocak: "CN",
            temmuz: "CO",
            ocakCoeffs: CoeffSet(column: "CN", memurK: 1.387871, tabanK: 22.722793, yanK: 0.440141, enYuksek: 13184.7745),
            temmuzCoeffs: CoeffSet(column: "CO", memurK: 1.48502197, tabanK: 24.31338851, yanK: 0.47095087, enYuksek: 14107.708715)
        )
    }

    func availableYears(activeYear: Int = 2026) -> [Int] {
        Array(Set(periods.compactMap(\.year)))
            .filter { (1997...activeYear).contains($0) }
            .sorted()
    }
}
