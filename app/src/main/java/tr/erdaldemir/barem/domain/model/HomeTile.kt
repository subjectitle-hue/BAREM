package tr.erdaldemir.barem.domain.model

import androidx.annotation.StringRes
import tr.erdaldemir.barem.R

enum class HomeTileStyle {
    Maas,
    Sozlesmeli,
    KamuIscisi,
    Emekli,
    AsgariUcret,
    Harcirah,
    YurtdisiMaas,
    DigerMaas,
    Tool,
}

enum class HomeTile(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val route: HomeRoute,
    val style: HomeTileStyle,
    val enabled: Boolean = true,
) {
    MAAS(R.string.tile_maas, R.string.tile_maas_sub, HomeRoute.Memur, HomeTileStyle.Maas),
    SOZLESMELI(R.string.tile_sozlesmeli, R.string.tile_sozlesmeli_sub, HomeRoute.SozlesmeliPlaceholder, HomeTileStyle.Sozlesmeli),
    KAMU_ISCISI(R.string.tile_kamu_iscisi, R.string.tile_kamu_iscisi_sub, HomeRoute.KamuIscisiPlaceholder, HomeTileStyle.KamuIscisi),
    EMEKLI(R.string.tile_emekli, R.string.tile_emekli_sub, HomeRoute.EmekliPlaceholder, HomeTileStyle.Emekli),
    ASGARI_UCRET(R.string.tile_asgari_ucret, R.string.tile_asgari_ucret_sub, HomeRoute.AsgariUcretPlaceholder, HomeTileStyle.AsgariUcret),
    HARCIRAH(R.string.tile_harcirah, R.string.tile_harcirah_sub, HomeRoute.HarcirahPlaceholder, HomeTileStyle.Harcirah),
    YURTDISI_MAAS(R.string.tile_yurtdisi_maas, R.string.tile_yurtdisi_maas_sub, HomeRoute.YurtdisiMaasPlaceholder, HomeTileStyle.YurtdisiMaas),
    DIGER_MAASLAR(R.string.tile_diger_maaslar, R.string.tile_diger_maaslar_sub, HomeRoute.DigerMaaslarPlaceholder, HomeTileStyle.DigerMaas),

    GRAFIK(R.string.tile_grafik, R.string.tile_grafik_sub, HomeRoute.SalaryCharts, HomeTileStyle.Tool),
    YILLIK(R.string.tile_yillik, R.string.tile_yillik_sub, HomeRoute.YearlyAverage, HomeTileStyle.Tool),
    ALTIN_DOLAR(R.string.tile_fx, R.string.tile_fx_sub, HomeRoute.GoldDollar, HomeTileStyle.Tool),
    GECMIS(R.string.tile_gecmis, R.string.tile_gecmis_sub, HomeRoute.History, HomeTileStyle.Tool),
    KARSILASTIRMA(R.string.tile_compare, R.string.tile_compare_sub, HomeRoute.Compare, HomeTileStyle.Tool),
    SENDIKA(R.string.tile_sendika, R.string.tile_sendika_sub, HomeRoute.AccountHub, HomeTileStyle.Tool),
    ;

    companion object {
        const val PRIMARY_COLUMNS = 2

        val primaryTiles: List<HomeTile> = listOf(
            MAAS,
            SOZLESMELI,
            KAMU_ISCISI,
            EMEKLI,
            ASGARI_UCRET,
            HARCIRAH,
            YURTDISI_MAAS,
            DIGER_MAASLAR,
        )
    }
}

private val bottomBarRoutes: Set<HomeRoute> = setOf(
    HomeRoute.AccountHub,
    HomeRoute.GorusHub,
    HomeRoute.GorusCategory,
    HomeRoute.GorusArticle,
    HomeRoute.History,
    HomeRoute.Compare,
    HomeRoute.StatDetail,
    HomeRoute.StatSalaryTable,
    HomeRoute.SalaryCharts,
    HomeRoute.YearlyAverage,
    HomeRoute.GoldDollar,
    HomeRoute.SendikaPanel,
)

/** Alt menü görünür mü? (Ana sayfa + sekme ekranları) */
fun HomeRoute?.showsBottomBar(): Boolean = this == null || this in bottomBarRoutes

enum class HomeRoute {
    Memur,
    SozlesmeliPlaceholder,
    KamuIscisiPlaceholder,
    EmekliPlaceholder,
    AsgariUcretPlaceholder,
    HarcirahPlaceholder,
    YurtdisiMaasPlaceholder,
    DigerMaaslarPlaceholder,
    AccountHub,
    VeriHub,
    GorusHub,
    GorusCategory,
    GorusArticle,
    SalaryCharts,
    YearlyAverage,
    GoldDollar,
    History,
    Compare,
    StatDetail,
    StatSalaryTable,
    SendikaPanel,
    ;

    companion object {
        fun fromSaved(name: String): HomeRoute? = when (name) {
            "IsciPlaceholder" -> KamuIscisiPlaceholder
            "DigerIslemPlaceholder" -> DigerMaaslarPlaceholder
            else -> entries.find { it.name == name }
        }
    }
}
