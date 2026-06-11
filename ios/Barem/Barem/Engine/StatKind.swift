import Foundation

enum StatKind: String, CaseIterable, Identifiable {
    case dollar
    case gold
    case asgari
    case memurRaise
    case asgariRaise
    case prim
    case gv
    case salaryTable

    var id: String { rawValue }

    var title: String {
        switch self {
        case .dollar: return "Dolar istatistikleri"
        case .gold: return "Altın istatistikleri"
        case .asgari: return "Asgari ücret istatistikleri"
        case .memurRaise: return "Memur maaş zamları"
        case .asgariRaise: return "Asgari ücret zamları"
        case .prim: return "Prim oranları"
        case .gv: return "Gelir vergisi dilimleri"
        case .salaryTable: return "2000–2026 Maaş İstatistiği"
        }
    }

    var subtitle: String {
        switch self {
        case .dollar: return "Net maaş / USD"
        case .gold: return "Net maaş / çeyrek altın"
        case .asgari: return "Yıllık net asgari ücret"
        case .memurRaise: return "Yıllık artış (%)"
        case .asgariRaise: return "Yıllık artış (%)"
        case .prim: return "SGK kesinti oranları"
        case .gv: return "Güncel dilim tutarları"
        case .salaryTable: return "Kadroya göre yıllık / aylık tablo"
        }
    }

    var showsChart: Bool {
        self != .prim && self != .gv
    }

    var chartTabIndex: Int {
        switch self {
        case .dollar: return 1
        case .gold: return 3
        case .memurRaise, .asgariRaise: return 4
        case .asgari: return 0
        default: return 0
        }
    }
}
