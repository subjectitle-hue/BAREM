import Foundation

// MARK: - TyoTier

enum TyoTier {
    static func indicator(ekGostergePuan: Double) -> Double {
        switch ekGostergePuan {
        case ..<0.01: return 55
        case 8400...: return 255
        case 7600...: return 215
        case 6400...: return 195
        case 4800...: return 165
        case 3600...: return 145
        case 2200...: return 85
        default: return 55
        }
    }
}

// MARK: - BlResolver

struct BlResolver {
    let repo: EngineRepository

    func resolve(form: MemurFormState, kadro: MKadroFullRow?, year: String) -> [Int: Double] {
        let derece = form.derece ?? 1
        let kademe = form.kademe ?? 1
        let kidemYili = form.kidemYili ?? 0
        let mRow = repo.mLookupForKadro(unvan: kadro?.unvan, detay: kadro?.detay, derece: kadro?.derece)
        let ekCode = mRow?.ekCode ?? kadro?.ekGosterge

        var bl: [Int: Double] = [:]
        bl[68] = repo.vLookupDereceKademe(derece: derece, kademe: kademe, year: year)
        bl[69] = repo.ekGostergePoints(ekCode: ekCode, derece: derece, year: year)
        bl[70] = kadro?.taban ?? 0
        bl[71] = kidemGosterge(kidemBirim: kadro?.kidem ?? 0, kidemYili: kidemYili)
        bl[72] = repo.vLookupPayIndicator(code: mRow?.tazminatCode, derece: derece, year: year)
        bl[73] = repo.vLookupPayIndicator(code: mRow?.ilaveTazPrimeDahilCode, derece: derece, year: year)
        bl[74] = repo.vLookupPayIndicator(code: mRow?.ilaveTazPrimeHaricCode, derece: derece, year: year)
        bl[75] = bolgeselTazminat(code: mRow?.bolgeselCode, bolgeselKod: form.bolgeselKod, year: year)
        bl[76] = repo.tsPrimeIndicatorForKadro(unvan: kadro?.unvan, detay: kadro?.detay, derece: kadro?.derece, year: year)
        bl[77] = 0
        bl[78] = repo.vLookupPayIndicator(code: mRow?.yanOdemeCode, derece: derece, year: year)
        bl[79] = 0
        bl[80] = 0
        bl[81] = repo.vLookupPayIndicator(code: mRow?.ekOdemeCode, derece: derece, year: year)
        for r in 82...103 { bl[r] = 0 }
        mRow?.payCodes?.forEach { row, code in
            guard let rowNum = Int(row), (82...103).contains(rowNum) else { return }
            bl[rowNum] = repo.vLookupPayIndicator(code: code, derece: derece, year: year)
        }
        applyYabanciDil(&bl, form: form, year: year)
        bl[122] = 707
        bl[124] = 2273
        bl[125] = 250
        bl[126] = 500
        bl[127] = TyoTier.indicator(ekGostergePuan: bl[69] ?? 0)
        let tazminatPath = (bl[72] ?? 0) > 0 || (bl[91] ?? 0) > 0 || (bl[105] ?? 0) > 0 || (bl[113] ?? 0) > 0
        bl[120] = repo.ilaveOdemeGostergesi(year: year, hasTazminatPath: tazminatPath)
        return bl
    }

    private func kidemGosterge(kidemBirim: Double, kidemYili: Int) -> Double {
        guard kidemBirim > 0 else { return 0 }
        let yil = kidemYili <= 0 ? 1 : min(kidemYili, 25)
        return min(500, kidemBirim * Double(yil))
    }

    private func bolgeselTazminat(code: String?, bolgeselKod: String?, year: String) -> Double {
        guard let code, !code.isEmpty, let bolgeselKod, !bolgeselKod.isEmpty else { return 0 }
        return repo.vLookupTable(key: "\(code)-\(bolgeselKod)", year: year)
    }

    private func applyYabanciDil(_ bl: inout [Int: Double], form: MemurFormState, year: String) {
        guard let level = form.yabanciDil else { return }
        let vKey: String?
        switch level.uppercased() {
        case "A", "A1": vKey = "A1"
        case "A2": vKey = "A2"
        case "B": vKey = "B"
        case "C": vKey = "C"
        default: vKey = nil
        }
        guard let vKey else { return }
        let points = repo.vLookupTable(key: vKey, year: year)
        if points > 0 { bl[79] = points }
    }
}

// MARK: - HPayEngine

enum HPayEngine {
    struct PayResult {
        let brut132: Double
        let bn127: Double
        let lines: [(String, Double)]
        let cells: [Int: Double]
    }

    static func compute(bl: [Int: Double], coeff: CoeffSet, form: MemurFormState? = nil) -> PayResult {
        let memurK = coeff.memurK
        let tabanK = coeff.tabanK
        let yanK = coeff.yanK
        let enY = coeff.enYuksek
        var bn: [Int: Double] = [:]

        bn[68] = r2((bl[68] ?? 0) * memurK)
        bn[69] = r2((bl[69] ?? 0) * memurK)
        bn[70] = r2((bl[70] ?? 0) * tabanK)
        bn[71] = r2((bl[71] ?? 0) * memurK)
        bn[72] = r2((bl[72] ?? 0) * enY / 100)
        bn[73] = r2((bl[73] ?? 0) * enY / 100)
        bn[74] = r2((bl[74] ?? 0) * enY / 100)
        bn[75] = r2((bl[75] ?? 0) * enY / 100)
        bn[76] = r2((bl[76] ?? 0) * enY / 100)
        bn[77] = r2((bl[77] ?? 0) * enY / 100)
        bn[78] = r2((bl[78] ?? 0) * yanK)
        bn[79] = r2((bl[79] ?? 0) * yanK)
        bn[80] = r2((bl[80] ?? 0) * yanK)
        bn[81] = r2((bl[81] ?? 0) * enY / 100)
        for r in 82...103 {
            let b = bl[r] ?? 0
            if b <= 0 { bn[r] = 0; continue }
            if r == 92 || r == 96 {
                bn[r] = r2((bn[68]! + bn[69]!) * b / 100)
            } else if (101...103).contains(r) {
                bn[r] = r2(b * memurK / 100)
            } else {
                bn[r] = r2(b * enY / 100)
            }
        }
        computeKistasBranch(bl: bl, bn: &bn, memurK: memurK)
        computeEk10Branch(bl: bl, bn: &bn, memurK: memurK)
        computeSpBranch(bl: bl, bn: &bn, memurK: memurK, enY: enY)
        let bl127 = bl[127] ?? 0
        bn[127] = bl127 > 0 ? r2(bl127 * enY / 100) : 0
        let baseBrut = selectBrutBase(bn)
        let brutExtras = brutRows118to126(bl: bl, bn: &bn, memurK: memurK, form: form)
        let brut = r2(baseBrut + brutExtras)
        let lines: [(String, Double)] = [
            ("Gösterge", bn[68] ?? 0), ("Taban", bn[70] ?? 0), ("Kıdem", bn[71] ?? 0),
            ("Tazminat", bn[72] ?? 0), ("Yan ödeme", bn[78] ?? 0), ("Ek ödeme", bn[81] ?? 0),
        ].filter { $0.1 > 0 }
        return PayResult(brut132: brut, bn127: bn[127] ?? 0, lines: lines, cells: bn)
    }

    private static func selectBrutBase(_ bn: [Int: Double]) -> Double {
        if (bn[105] ?? 0) > 0 { return r2((105...109).reduce(0) { $0 + (bn[$1] ?? 0) }) }
        if (bn[113] ?? 0) > 0 { return r2((113...117).reduce(0) { $0 + (bn[$1] ?? 0) }) }
        if (bn[128] ?? 0) > 0 { return r2((128...131).reduce(0) { $0 + (bn[$1] ?? 0) }) }
        return r2((68...103).reduce(0) { $0 + (bn[$1] ?? 0) })
    }

    private static func computeKistasBranch(bl: [Int: Double], bn: inout [Int: Double], memurK: Double) {
        guard (bl[105] ?? 0) > 0 else { return }
        bn[110] = r2((bl[110] ?? 0) * memurK)
        bn[105] = r2((bl[105] ?? 0) / 100 * bn[110]!)
        bn[106] = r2((bl[106] ?? 0) / 100 * bn[110]!)
        bn[107] = r2((bl[107] ?? 0) / 100 * bn[105]!)
        bn[108] = r2((bl[108] ?? 0) * memurK)
        bn[109] = r2((bl[109] ?? 0) * memurK)
    }

    private static func computeEk10Branch(bl: [Int: Double], bn: inout [Int: Double], memurK: Double) {
        guard (bl[113] ?? 0) > 0 else { return }
        for r in 113...117 {
            let cm = bl[r] ?? 0
            bn[r] = cm > 0 ? r2(cm * memurK) : 0
        }
    }

    private static func computeSpBranch(bl: [Int: Double], bn: inout [Int: Double], memurK: Double, enY: Double) {
        guard (bl[128] ?? 0) > 0 else { return }
        bn[128] = r2((bl[128] ?? 0) * (enY / 100 + 1))
        bn[129] = r2((bl[129] ?? 0) * enY / 100)
        bn[130] = r2((bl[130] ?? 0) * enY / 100)
        bn[131] = r2((bl[131] ?? 0) * enY / 100)
    }

    private static func brutRows118to126(bl: [Int: Double], bn: inout [Int: Double], memurK: Double, form: MemurFormState?) -> Double {
        var extra = 0.0
        if (bl[120] ?? 0) > 0 {
            let pay = r2((bl[120] ?? 0) * memurK)
            bn[120] = pay; extra += pay
        }
        if form?.topluSozlesme == "VAR" {
            let pay = r2((bl[122] ?? 707) * memurK)
            bn[122] = pay; extra += pay
        }
        if form?.medeniHal == "Evli-Eşi Çalışmayan" {
            let pay = r2((bl[124] ?? 2273) * memurK)
            bn[124] = pay; extra += pay
        }
        let ust6 = form?.cocukUst6 ?? 0
        if ust6 > 0 {
            let pay = r2((bl[125] ?? 250) * memurK * Double(ust6))
            bn[125] = pay; extra += pay
        }
        let alt6 = form?.cocukAlt6 ?? 0
        if alt6 > 0 {
            let pay = r2((bl[126] ?? 500) * memurK * Double(alt6))
            bn[126] = pay; extra += pay
        }
        return extra
    }

    private static func r2(_ v: Double) -> Double { (v * 100).rounded() / 100 }
}

// MARK: - GV / Damga

enum GvCalculator {
    static func cumulativeTax(bl: Double, b: GvBrackets) -> Double {
        let limits = b.dilimLimits
        let base = b.dilimBaseTax
        let rates = b.dilimRates
        if bl > limits[3] { return (bl - limits[3]) * rates[4] + base[3] }
        if bl > limits[2] { return (bl - limits[2]) * rates[3] + base[2] }
        if bl > limits[1] { return (bl - limits[1]) * rates[2] + base[1] }
        if bl > limits[0] { return (bl - limits[0]) * rates[1] + base[0] }
        return bl * rates[0]
    }

    static func monthlyGv(bm140Ocak: Double, bm140Tem: Double, agi: Double, b: GvBrackets) -> [Double] {
        var gv = [Double](repeating: 0, count: 12)
        var prevTax = 0.0
        for m in 0..<12 {
            let cumBl = m < 6 ? bm140Ocak * Double(m + 1) : bm140Ocak * 6 + bm140Tem * Double(m - 5)
            let tax = cumulativeTax(bl: cumBl, b: b)
            gv[m] = r2(max(0, (tax - prevTax) - agi))
            prevTax = tax
        }
        return gv
    }

    private static func r2(_ v: Double) -> Double { (v * 100).rounded() / 100 }
}

enum DamgaCalculator {
    static func monthlyDamga(brutOcak: Double, brutTem: Double, rate: Double, muafBm: Double, muafBn: Double) -> [Double] {
        var dv = [Double](repeating: 0, count: 12)
        var prevBm = 0.0
        for m in 0..<12 {
            let cumBrut = m < 6 ? brutOcak * Double(m + 1) : brutOcak * 6 + brutTem * Double(m - 5)
            let bm = r2(cumBrut * rate)
            let muaf = m < 6 ? muafBm : muafBn
            dv[m] = r2(max(0, (bm - prevBm) - muaf))
            prevBm = bm
        }
        return dv
    }

    private static func r2(_ v: Double) -> Double { (v * 100).rounded() / 100 }
}

enum CrossColumnNetCalculator {
    static func monthlyNet(
        payOcak: HPayEngine.PayResult, payTem: HPayEngine.PayResult,
        primOcak: Double, primTem: Double,
        gvMatrahOcak: Double, gvMatrahTem: Double,
        brackets: GvBrackets, thresholds: CrossColumnThresholds
    ) -> [Double] {
        var cnTax = [Double](repeating: 0, count: 12)
        for m in 0..<6 { cnTax[m] = GvCalculator.cumulativeTax(bl: gvMatrahOcak * Double(m + 1), b: brackets) }
        for m in 0..<6 {
            cnTax[6 + m] = GvCalculator.cumulativeTax(bl: gvMatrahOcak * 6 + gvMatrahTem * Double(m + 1), b: brackets)
        }
        let cnDvCum = cumulativeDamga(brutOcak: payOcak.brut132, brutTem: payTem.brut132, rate: brackets.damgaRate)
        var nets = [Double](repeating: 0, count: 12)
        for m in 0..<6 {
            let gv = m == 0 ? max(0, cnTax[0] - thresholds.gvFirstHalf[m])
                : max(0, cnTax[m] - cnTax[m - 1] - thresholds.gvFirstHalf[m])
            let dv = m == 0 ? max(0, cnDvCum[m] - thresholds.dvMuafOcak)
                : max(0, cnDvCum[m] - cnDvCum[m - 1] - thresholds.dvMuafOcak)
            nets[m] = r2(payOcak.brut132 - primOcak - gv - dv)
        }
        for m in 0..<6 {
            let taxIdx = 6 + m
            let gv = m == 0 ? max(0, cnTax[taxIdx] - cnTax[5] - thresholds.gvSecondHalf[m])
                : max(0, cnTax[taxIdx] - cnTax[taxIdx - 1] - thresholds.gvSecondHalf[m])
            let dv = m == 0 ? max(0, cnDvCum[6 + m] - cnDvCum[5 + m] - thresholds.dvMuafTem)
                : max(0, cnDvCum[6 + m] - cnDvCum[5 + m] - thresholds.dvMuafTem)
            nets[6 + m] = r2(payTem.brut132 - primTem - gv - dv)
        }
        return nets
    }

    private static func cumulativeDamga(brutOcak: Double, brutTem: Double, rate: Double) -> [Double] {
        var cum = [Double](repeating: 0, count: 12)
        for m in 0..<6 { cum[m] = r2(brutOcak * Double(m + 1) * rate) }
        for m in 0..<6 { cum[6 + m] = r2((brutOcak * 6 + brutTem * Double(m + 1)) * rate) }
        return cum
    }

    private static func r2(_ v: Double) -> Double { (v * 100).rounded() / 100 }
}

// MARK: - HDeductionEngine

struct HDeductionEngine {
    let gvBrackets: GvBrackets

    struct DeductionResult {
        let primOcak: Double
        let primTem: Double
        let gvMonthly: [Double]
        let dvMonthly: [Double]
        let gvMatrahOcak: Double
        let gvMatrahTem: Double
    }

    func compute(
        payOcak: HPayEngine.PayResult, payTem: HPayEngine.PayResult,
        sgkTipi: String, primOran5434: Double, primOran5510: Double, dvMuafiyetIndex: Double
    ) -> DeductionResult {
        let primRate = sgkTipi == "5434" ? primOran5434 : primOran5510
        let primOcak = r2(primMatrah(payOcak) * primRate / 100)
        let primTem = r2(primMatrah(payTem) * primRate / 100)
        let gvMatrahOcak = computeGvMatrah(pay: payOcak, prim: primOcak)
        let gvMatrahTem = computeGvMatrah(pay: payTem, prim: primTem)
        let gv = GvCalculator.monthlyGv(bm140Ocak: gvMatrahOcak, bm140Tem: gvMatrahTem, agi: gvBrackets.agiMonthly, b: gvBrackets)
        let dv = DamgaCalculator.monthlyDamga(
            brutOcak: payOcak.brut132, brutTem: payTem.brut132,
            rate: gvBrackets.damgaRate, muafBm: gvBrackets.damgaMuafiyetBm ?? 0, muafBn: dvMuafiyetIndex
        )
        return DeductionResult(primOcak: primOcak, primTem: primTem, gvMonthly: gv, dvMonthly: dv,
                               gvMatrahOcak: gvMatrahOcak, gvMatrahTem: gvMatrahTem)
    }

    private func primMatrah(_ pay: HPayEngine.PayResult) -> Double {
        r2(pay.bn127 + (pay.cells[68] ?? 0) + (pay.cells[69] ?? 0) + (pay.cells[70] ?? 0) + (pay.cells[71] ?? 0))
    }

    private func computeGvMatrah(pay: HPayEngine.PayResult, prim: Double) -> Double {
        let c = pay.cells
        let base: Double
        if (c[113] ?? 0) > 0 { base = (c[113] ?? 0) + (c[114] ?? 0) }
        else if (c[111] ?? 0) > 0 { base = c[111] ?? 0 }
        else if (c[128] ?? 0) > 0 { base = c[128] ?? 0 }
        else { base = [68, 69, 70, 71, 78, 79, 80, 97, 98, 99, 100, 101, 102, 103].reduce(0) { $0 + (c[$1] ?? 0) } + (c[118] ?? 0) }
        return r2(max(0, base - prim))
    }

    private func r2(_ v: Double) -> Double { (v * 100).rounded() / 100 }
}

// MARK: - FxOutputCalculator

enum FxOutputCalculator {
    static func compute(monthlyNet: [Double], rates: FxSemesterRates) -> FxOutput? {
        guard monthlyNet.count >= 12 else { return nil }
        let usd = monthlyNet.enumerated().map { i, net in
            convert(net, rate: i < 6 ? rates.usdOcak : rates.usdTemmuz)
        }
        let eur = monthlyNet.enumerated().map { i, net in
            convert(net, rate: i < 6 ? rates.eurOcak : rates.eurTemmuz)
        }
        let gold = monthlyNet.map { convert($0, rate: rates.goldQuarter) }
        return FxOutput(
            monthlyUsd: usd, monthlyEur: eur, monthlyGoldQuarter: gold,
            yearlyAvgUsd: r2(usd.reduce(0, +) / Double(usd.count)),
            yearlyAvgEur: r2(eur.reduce(0, +) / Double(eur.count)),
            yearlyAvgGoldQuarter: r2(gold.reduce(0, +) / Double(gold.count))
        )
    }

    private static func convert(_ net: Double, rate: Double?) -> Double {
        guard let rate, rate > 0 else { return 0 }
        return r2(net / rate)
    }

    private static func r2(_ v: Double) -> Double { (v * 100).rounded() / 100 }
}

// MARK: - ExcelMemurEngine

final class ExcelMemurEngine {
    private let repo = EngineRepository.shared
    private let blResolver = BlResolver(repo: EngineRepository.shared)
    private let deductionEngine: HDeductionEngine

    init() {
        deductionEngine = HDeductionEngine(gvBrackets: repo.brackets())
    }

    func calculate(form: MemurFormState, kadro: MKadroFullRow?) -> SalaryResult? {
        let year = form.hesapYili ?? repo.activeYear
        let pair = repo.semesterPairForYear(year) ?? repo.activeSemesterPair()
        return calculateForPeriod(form: form, kadro: kadro, year: year, pair: pair)
    }

    func calculateForPeriod(form: MemurFormState, kadro: MKadroFullRow?, year: Int, pair: SemesterColumnPair) -> SalaryResult {
        let period = "\(year)-\(pair.ocakCoeffs.memurK)"
        let gvMeta = repo.gvMeta
        let brackets = repo.brackets()
        let crossColumn = brackets.deductionMode == "crossColumn" || gvMeta.deductionMode == "crossColumn"
        let bl = blResolver.resolve(form: form, kadro: kadro, year: String(year))
        let payOcak = HPayEngine.compute(bl: bl, coeff: pair.ocakCoeffs, form: form)
        let payTem = HPayEngine.compute(bl: bl, coeff: pair.temmuzCoeffs, form: form)
        let ded = deductionEngine.compute(
            payOcak: payOcak, payTem: payTem, sgkTipi: gvMeta.sgkTipi,
            primOran5434: gvMeta.primOran5434, primOran5510: gvMeta.primOran5510,
            dvMuafiyetIndex: repo.indexLookup(label: "DV Muafiyet", period: period)
        )
        let monthlyNet: [Double]
        if crossColumn, let thresholds = brackets.crossColumnThresholds {
            monthlyNet = CrossColumnNetCalculator.monthlyNet(
                payOcak: payOcak, payTem: payTem, primOcak: ded.primOcak, primTem: ded.primTem,
                gvMatrahOcak: ded.gvMatrahOcak, gvMatrahTem: ded.gvMatrahTem,
                brackets: brackets, thresholds: thresholds
            )
        } else {
            monthlyNet = (0..<12).map { m in
                let brut = m < 6 ? payOcak.brut132 : payTem.brut132
                let prim = m < 6 ? ded.primOcak : ded.primTem
                return r2(brut - prim - ded.gvMonthly[m] - ded.dvMonthly[m])
            }
        }
        let aileBrut = r2((payTem.cells[124] ?? 0) + (payTem.cells[125] ?? 0) + (payTem.cells[126] ?? 0))
        let avgNet = r2(monthlyNet.reduce(0, +) / Double(monthlyNet.count))
        let firstHalfAvg = r2(monthlyNet.prefix(6).reduce(0, +) / 6)
        let secondHalfAvg = r2(monthlyNet.suffix(6).reduce(0, +) / 6)
        let yillikNet = r2(monthlyNet.reduce(0, +))
        let yillikBrut = r2(payOcak.brut132 * 6 + payTem.brut132 * 6)
        let kesintiAylik = r2(monthlyNet.indices.map { i in
            let prim = i < 6 ? ded.primOcak : ded.primTem
            return prim + ded.gvMonthly[i] + ded.dvMonthly[i]
        }.reduce(0, +) / 12)
        let gold = repo.goldRate(period: period)
        let usd = repo.usdRate(period: period)
        let fx = repo.fxRatesForYear(year).flatMap { FxOutputCalculator.compute(monthlyNet: monthlyNet, rates: $0) }
        return SalaryResult(
            brutAylik: r2(payTem.brut132), kesintiler: kesintiAylik, netAylik: avgNet,
            cocukYardimi: aileBrut, yillikBrut: yillikBrut, yillikNet: yillikNet,
            firstHalfAvg: firstHalfAvg, secondHalfAvg: secondHalfAvg,
            altinGramKarsilik: gold > 0 ? r2(avgNet / gold * 10) / 10 : 0,
            dolarKarsilik: usd > 0 ? r2(avgNet / usd * 100) / 100 : 0,
            periodLabel: "\(year) (Excel H!\(pair.ocak)/\(pair.temmuz))",
            monthlyNet: monthlyNet,
            fxOutput: fx
        )
    }

    private func r2(_ v: Double) -> Double { (v * 100).rounded() / 100 }
}
