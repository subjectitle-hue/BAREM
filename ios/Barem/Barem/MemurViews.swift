import SwiftUI

enum MemurWizardStep: Int, CaseIterable {
    case hizmetSinifi = 0
    case kadro = 1
    case kisisel = 2
    case sonuc = 3

    var title: String {
        switch self {
        case .hizmetSinifi: return "Hizmet sınıfı"
        case .kadro: return "Maaş unsurları"
        case .kisisel: return "Kişisel bilgiler"
        case .sonuc: return "Sonuç"
        }
    }
}

@MainActor
final class MemurWizardModel: ObservableObject {
    @Published var step: MemurWizardStep = .hizmetSinifi
    @Published var form = MemurFormState()
    @Published var salaryResult: SalaryResult?

    private let catalog = MemurCatalog.shared
    private let engine = ExcelMemurEngine()
    private let repo = EngineRepository.shared

    func hizmetSiniflari() -> [String] { catalog.hizmetSiniflari() }
    func hesapYillari() -> [Int] { catalog.hesapYillari(activeYear: repo.activeYear) }
    func effectiveHesapYili() -> Int { form.hesapYili ?? repo.activeYear }

    func selectHizmetSinifi(_ code: String) {
        form.hizmetSinifi = code
        form.unvan = nil; form.kadroDetay = nil; form.derece = nil; form.kademe = nil
    }

    func searchUnvanlar(_ query: String) -> [String] {
        guard let sinif = form.hizmetSinifi else { return [] }
        return catalog.searchUnvanlar(hizmetSinifi: sinif, query: query)
    }

    func detaylar() -> [String] {
        guard let sinif = form.hizmetSinifi, let unvan = form.unvan else { return [] }
        return catalog.detaylar(hizmetSinifi: sinif, unvan: unvan)
    }

    func showDetayField() -> Bool {
        guard let sinif = form.hizmetSinifi else { return false }
        return catalog.showDetayField(hizmetSinifi: sinif, unvan: form.unvan)
    }

    func dereceler() -> [Int] {
        guard let sinif = form.hizmetSinifi, let unvan = form.unvan else { return [] }
        let detay = showDetayField() ? form.kadroDetay : nil
        return catalog.dereceler(hizmetSinifi: sinif, unvan: unvan, detay: detay)
    }

    func kademeSecenekleri() -> [Int] { Array(1...9) }
    func kidemSecenekleri() -> [Int] { Array(0...25) }

    func canContinue() -> Bool {
        switch step {
        case .hizmetSinifi: return form.hizmetSinifi != nil
        case .kadro:
            return form.unvan != nil && form.derece != nil && form.kademe != nil && form.kidemYili != nil &&
                (!showDetayField() || form.kadroDetay != nil)
        case .kisisel: return true
        case .sonuc: return true
        }
    }

    func goNext() {
        if step == .kisisel { calculate() }
        guard let next = MemurWizardStep(rawValue: step.rawValue + 1) else { return }
        step = next
    }

    func goBack() -> Bool {
        guard let prev = MemurWizardStep(rawValue: step.rawValue - 1) else { return false }
        step = prev
        return true
    }

    private func calculate() {
        guard let sinif = form.hizmetSinifi, let unvan = form.unvan, let derece = form.derece else { return }
        let detay = showDetayField() ? (form.kadroDetay ?? "") : ""
        let kadro = repo.findKadroFull(hizmetSinifi: sinif, unvan: unvan, detay: detay, derece: derece)
        if let result = engine.calculate(form: form, kadro: kadro) {
            salaryResult = result
            CalcSession.shared.save(form: form, result: result, year: effectiveHesapYili())
            Task { await CalcSession.shared.refreshYearlySeriesIfNeeded() }
        }
    }

    func restoreAndCalculate(_ restored: MemurFormState) {
        form = restored
        calculate()
        step = .sonuc
    }
}

struct MemurWizardView: View {
    var restoreForm: MemurFormState?
    @StateObject private var model = MemurWizardModel()
    @Environment(\.dismiss) private var dismiss
    @ObservedObject private var premium = PremiumManager.shared

    var body: some View {
        VStack(spacing: 0) {
            ProgressView(value: Double(model.step.rawValue + 1), total: Double(MemurWizardStep.allCases.count))
                .padding(.horizontal)
            Group {
                switch model.step {
                case .hizmetSinifi: HizmetSinifiStep(model: model)
                case .kadro: KadroStep(model: model)
                case .kisisel: KisiselStep(model: model)
                case .sonuc:
                    if let result = model.salaryResult {
                        ResultView(result: result)
                    } else {
                        Text("Hesap yapılamadı. Seçimleri kontrol edin.").padding()
                    }
                }
            }
            if model.step != .sonuc && model.step != .hizmetSinifi {
                HStack {
                    Spacer()
                    Button("Devam") { model.goNext() }
                        .buttonStyle(.borderedProminent)
                        .disabled(!model.canContinue())
                }
                .padding()
            }
        }
        .navigationTitle("Memur")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button("Geri") {
                    if !model.goBack() { dismiss() }
                }
            }
        }
        .onAppear {
            if let restoreForm {
                model.restoreAndCalculate(restoreForm)
            }
        }
        .onChange(of: premium.isPremium) { isOn in
            if isOn { Task { await CalcSession.shared.refreshYearlySeriesIfNeeded() } }
        }
    }
}

private struct HizmetSinifiStep: View {
    @ObservedObject var model: MemurWizardModel

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 8) {
                Text("Hizmet sınıfı seçildikten sonra maaş unsurlarına geçilir.")
                    .font(.subheadline).foregroundStyle(.secondary)
                ForEach(model.hizmetSiniflari(), id: \.self) { code in
                    Button {
                        model.selectHizmetSinifi(code)
                        model.goNext()
                    } label: {
                        BaremSelectionCard(
                            title: MemurCatalog.shared.hizmetSinifiDisplay(code),
                            selected: model.form.hizmetSinifi == code
                        )
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding()
        }
    }
}

private struct KadroStep: View {
    @ObservedObject var model: MemurWizardModel
    @State private var unvanQuery = ""

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Picker("Hesap yılı", selection: Binding(
                    get: { model.effectiveHesapYili() },
                    set: { model.form.hesapYili = $0 }
                )) {
                    ForEach(model.hesapYillari(), id: \.self) { y in
                        Text(String(y)).tag(y)
                    }
                }

                TextField("Unvan ara…", text: $unvanQuery)
                    .textFieldStyle(.roundedBorder)
                ForEach(model.searchUnvanlar(unvanQuery).prefix(20), id: \.self) { unvan in
                    Button {
                        model.form.unvan = unvan
                        model.form.kadroDetay = nil
                        model.form.derece = nil
                    } label: {
                        BaremSelectionCard(title: unvan, selected: model.form.unvan == unvan, subtitle: nil)
                    }
                    .buttonStyle(.plain)
                }

                if model.showDetayField() {
                    Picker("Detay", selection: Binding(
                        get: { model.form.kadroDetay ?? "" },
                        set: { model.form.kadroDetay = $0 }
                    )) {
                        Text("Seçiniz…").tag("")
                        ForEach(model.detaylar(), id: \.self) { d in Text(d).tag(d) }
                    }
                }

                if !model.dereceler().isEmpty {
                    Picker("Derece", selection: Binding(
                        get: { model.form.derece ?? 0 },
                        set: { model.form.derece = $0 }
                    )) {
                        Text("Seçiniz…").tag(0)
                        ForEach(model.dereceler(), id: \.self) { d in Text(String(d)).tag(d) }
                    }
                }

                if model.form.derece != nil {
                    Picker("Kademe", selection: Binding(
                        get: { model.form.kademe ?? 1 },
                        set: { model.form.kademe = $0 }
                    )) {
                        ForEach(model.kademeSecenekleri(), id: \.self) { k in Text(String(k)).tag(k) }
                    }
                    Picker("Kıdem yılı", selection: Binding(
                        get: { model.form.kidemYili ?? 0 },
                        set: { model.form.kidemYili = $0 }
                    )) {
                        ForEach(model.kidemSecenekleri(), id: \.self) { k in Text(String(k)).tag(k) }
                    }
                }
            }
            .padding()
        }
    }
}

private struct KisiselStep: View {
    @ObservedObject var model: MemurWizardModel
    private let catalog = MemurCatalog.shared
    private let vd = VdLookupRepository.shared

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Picker("Medeni hal", selection: Binding(
                    get: { model.form.medeniHal ?? "" },
                    set: { model.form.medeniHal = $0.isEmpty ? nil : $0 }
                )) {
                    Text("Seçiniz…").tag("")
                    ForEach(catalog.medeniHalSecenekleri(), id: \.self) { o in Text(o).tag(o) }
                }
                Picker("Toplu sözleşme ikramiyesi", selection: Binding(
                    get: { model.form.topluSozlesme == "VAR" ? "Evet" : "Hayır" },
                    set: { model.form.topluSozlesme = $0 == "Evet" ? "VAR" : "YOK" }
                )) {
                    Text("Hayır").tag("Hayır")
                    Text("Evet").tag("Evet")
                }
                Picker("Yabancı dil seviyesi", selection: Binding(
                    get: { model.form.yabanciDil ?? "" },
                    set: { model.form.yabanciDil = $0.isEmpty ? nil : $0 }
                )) {
                    Text("Yok").tag("")
                    ForEach(catalog.yabanciDilSecenekleri(), id: \.self) { o in Text(o).tag(o) }
                }
                Picker("İl", selection: Binding(
                    get: { model.form.il ?? "" },
                    set: {
                        model.form.il = $0.isEmpty ? nil : $0
                        model.form.ilce = nil
                        model.form.bolgeselKod = nil
                    }
                )) {
                    Text("Seçiniz…").tag("")
                    ForEach(vd.iller(), id: \.self) { il in Text(il).tag(il) }
                }
                if let il = model.form.il {
                    Picker("İlçe", selection: Binding(
                        get: { model.form.ilce ?? "" },
                        set: {
                            model.form.ilce = $0.isEmpty ? nil : $0
                            if let ilce = model.form.ilce {
                                model.form.bolgeselKod = vd.bolgeselKod(il: il, ilce: ilce)
                            }
                        }
                    )) {
                        Text("Seçiniz…").tag("")
                        ForEach(vd.ilceler(il: il), id: \.self) { i in Text(i).tag(i) }
                    }
                }
                Stepper("6 yaş üstü çocuk: \(model.form.cocukUst6)", value: $model.form.cocukUst6, in: 0...10)
                Stepper("6 yaş altı çocuk: \(model.form.cocukAlt6)", value: $model.form.cocukAlt6, in: 0...10)
            }
            .padding()
        }
    }
}

struct ResultView: View {
    let result: SalaryResult
    @State private var currency: ResultCurrency = .tl

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                Picker("Para birimi", selection: $currency) {
                    ForEach(ResultCurrency.allCases, id: \.self) { c in
                        Text(c.label).tag(c)
                    }
                }
                .pickerStyle(.segmented)

                monthlyGrid

                resultStatBox(title: "Yıllık Ortalama", value: formatAmount(stats.yearlyAvg), highlight: true)

                NavigationLink {
                    YearlyBordroView(result: result)
                } label: {
                    hubTile("BORDRO (Yıllık)")
                }

                NavigationLink {
                    SalaryTableView()
                } label: {
                    hubTile("2000–2026 Maaş İstatistiği")
                }
            }
            .padding()
        }
        .navigationTitle("Maaş sonucu")
    }

    private var stats: (yearlyAvg: Double, firstHalf: Double, secondHalf: Double) {
        switch currency {
        case .tl: return (result.netAylik, result.firstHalfAvg, result.secondHalfAvg)
        case .usd: return (result.dolarKarsilik, result.dolarKarsilik, result.dolarKarsilik)
        case .eur: return (result.dolarKarsilik * 0.92, result.dolarKarsilik * 0.92, result.dolarKarsilik * 0.92)
        case .gold: return (result.altinGramKarsilik, result.altinGramKarsilik, result.altinGramKarsilik)
        }
    }

    private var monthlyGrid: some View {
        VStack(spacing: 8) {
            ForEach(0..<4, id: \.self) { row in
                HStack(spacing: 8) {
                    ForEach(0..<3, id: \.self) { col in
                        let idx = row * 3 + col
                        if idx < result.monthlyNet.count {
                            monthCell(
                                month: SalaryResult.monthLabels[idx],
                                amount: formatAmount(monthlyValue(idx))
                            )
                        } else {
                            Color.clear.frame(maxWidth: .infinity)
                        }
                    }
                }
            }
        }
    }

    private func monthlyValue(_ index: Int) -> Double {
        let net = result.monthlyNet[index]
        switch currency {
        case .tl: return net
        case .usd: return result.dolarKarsilik
        case .eur: return result.dolarKarsilik * 0.92
        case .gold: return result.altinGramKarsilik
        }
    }

    private func monthCell(month: String, amount: String) -> some View {
        VStack(spacing: 4) {
            Text(month).font(.caption).foregroundStyle(.secondary)
            Text(amount).font(.caption.bold()).multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 10)
        .background(Color.blue.opacity(0.08))
        .clipShape(RoundedRectangle(cornerRadius: 10))
    }

    private func resultStatBox(title: String, value: String, highlight: Bool) -> some View {
        VStack(spacing: 8) {
            Text(title).font(.subheadline)
            Text(value).font(.title2.bold())
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(highlight ? Color.red.opacity(0.85) : Color.gray.opacity(0.15))
        .foregroundStyle(highlight ? .white : .primary)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    private func hubTile(_ title: String) -> some View {
        Text(title)
            .font(.headline)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 18)
            .background(Color.blue.opacity(0.15))
            .clipShape(RoundedRectangle(cornerRadius: 14))
    }

    private func formatAmount(_ v: Double) -> String {
        switch currency {
        case .tl: return "\(baremFormat(v)) ₺"
        case .usd: return "$\(baremFormat(v))"
        case .eur: return "\(baremFormat(v)) €"
        case .gold: return "\(baremFormat(v)) çeyrek"
        }
    }
}
