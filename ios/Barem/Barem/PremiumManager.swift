import Foundation
import SwiftUI
import StoreKit

/// Android BillingEntitlementRepository — StoreKit 2 + debug grant.
@MainActor
final class PremiumManager: ObservableObject {
    static let shared = PremiumManager()
    static let productId = "Barem_premium"

    @Published private(set) var isPremium = false
    @Published var purchaseMessage: String?

    private var transactionListener: Task<Void, Never>?

    private init() {
        transactionListener = Task {
            for await result in Transaction.updates {
                guard case .verified(let transaction) = result else { continue }
                await transaction.finish()
                await refreshEntitlement()
            }
        }
        Task { await refreshEntitlement() }
    }

    func start() async {
        await refreshEntitlement()
    }

    func purchase() {
        Task { await purchaseProduct() }
    }

    func restore() {
        Task { await refreshEntitlement() }
    }

    func requiresPremium(for feature: PremiumFeature) -> Bool {
        switch feature {
        case .yearlySeries, .salaryTable: return !isPremium
        case .statistics: return false
        }
    }

    private func purchaseProduct() async {
        do {
            let products = try await Product.products(for: [Self.productId])
            guard let product = products.first else {
                purchaseMessage = "Ürün App Store Connect'te yapılandırılmamış (\(Self.productId)). Debug'da test premium kullanın."
                return
            }
            let result = try await product.purchase()
            switch result {
            case .success(let verification):
                if case .verified(let transaction) = verification {
                    await transaction.finish()
                    await refreshEntitlement()
                }
            case .userCancelled, .pending:
                break
            @unknown default:
                break
            }
        } catch {
            purchaseMessage = error.localizedDescription
        }
    }

    func refreshEntitlement() async {
        var premium = UserDefaults.standard.bool(forKey: "barem_debug_premium")
        for await result in Transaction.currentEntitlements {
            if case .verified(let txn) = result, txn.productID == Self.productId {
                premium = true
            }
        }
        let wasPremium = isPremium
        isPremium = premium
        if premium && !wasPremium {
            await CalcSession.shared.refreshYearlySeriesIfNeeded()
        }
        if !premium {
            CalcSession.shared.clearYearlySeries()
        }
    }

    #if DEBUG
    func debugGrantPremium() {
        UserDefaults.standard.set(true, forKey: "barem_debug_premium")
        isPremium = true
        Task { await CalcSession.shared.refreshYearlySeriesIfNeeded() }
    }

    func debugRevokePremium() {
        UserDefaults.standard.set(false, forKey: "barem_debug_premium")
        isPremium = false
        CalcSession.shared.clearYearlySeries()
    }
    #endif
}

enum PremiumFeature {
    case yearlySeries
    case salaryTable
    case statistics
}

struct PremiumUpsellView: View {
    @ObservedObject private var premium = PremiumManager.shared

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Premium ile açın")
                .font(.headline)
                .foregroundStyle(.orange)
            Text("1997–2026 yıllık net serisi, FX tabloları, 6 grafik ve yıl karşılaştırması.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
            if let msg = premium.purchaseMessage {
                Text(msg).font(.caption).foregroundStyle(.red)
            }
            Button("Premium satın al") { premium.purchase() }
                .buttonStyle(.borderedProminent)
                .frame(maxWidth: .infinity)
            Button("Satın almayı geri yükle") { premium.restore() }
                .buttonStyle(.bordered)
                .frame(maxWidth: .infinity)
            #if DEBUG
            Divider()
            Text("Geliştirici testi (debug)").font(.caption).foregroundStyle(.secondary)
            HStack {
                Button("Premium aç (test)") { premium.debugGrantPremium() }
                Button("Premium kapat (test)") { premium.debugRevokePremium() }
            }
            .font(.caption)
            Text("App Store ürün kimliği: \(PremiumManager.productId)")
                .font(.caption2)
                .foregroundStyle(.secondary)
            #endif
        }
        .padding()
        .background(Color.orange.opacity(0.08))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

struct PremiumGate<Preview: View, Content: View>: View {
    @ObservedObject private var premium = PremiumManager.shared
    @ViewBuilder let preview: () -> Preview
    @ViewBuilder let content: () -> Content

    var body: some View {
        if premium.isPremium {
            content()
        } else {
            preview()
            PremiumUpsellView()
        }
    }
}

struct SendikaPanelView: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Sendika paneli")
                .font(.headline)
            Text("Sendika üyeleri için tüm grafikler, yıllık ortalamalar, altın/dolar kıyasları ve geçmiş hesaplar bu sürümde açıktır.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
            PremiumUpsellView()
        }
        .padding(.vertical, 8)
    }
}
