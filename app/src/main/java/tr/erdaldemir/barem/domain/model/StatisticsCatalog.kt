package tr.erdaldemir.barem.domain.model

import androidx.annotation.StringRes
import tr.erdaldemir.barem.R

enum class StatKind {
    DOLLAR,
    GOLD,
    ASGARI,
    MEMUR_RAISE,
    ASGARI_RAISE,
    PRIM_RATES,
    GV_BRACKETS,
    SALARY_TABLE,
}

data class StatMenuItem(
    val kind: StatKind,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
)

object StatisticsCatalog {
    val menuItems: List<StatMenuItem> = listOf(
        StatMenuItem(StatKind.DOLLAR, R.string.stat_menu_dollar, R.string.stat_menu_dollar_sub),
        StatMenuItem(StatKind.GOLD, R.string.stat_menu_gold, R.string.stat_menu_gold_sub),
        StatMenuItem(StatKind.ASGARI, R.string.stat_menu_asgari, R.string.stat_menu_asgari_sub),
        StatMenuItem(StatKind.MEMUR_RAISE, R.string.stat_menu_memur_raise, R.string.stat_menu_memur_raise_sub),
        StatMenuItem(StatKind.ASGARI_RAISE, R.string.stat_menu_asgari_raise, R.string.stat_menu_asgari_raise_sub),
        StatMenuItem(StatKind.PRIM_RATES, R.string.stat_menu_prim, R.string.stat_menu_prim_sub),
        StatMenuItem(StatKind.GV_BRACKETS, R.string.stat_menu_gv, R.string.stat_menu_gv_sub),
        StatMenuItem(StatKind.SALARY_TABLE, R.string.stat_menu_salary_table, R.string.stat_menu_salary_table_sub),
    )
}

object StatisticsNavStore {
    var selectedKind: StatKind? = null
    var chartTabIndex: Int = 0
}

object GorusNavStore {
    var categoryId: String? = null
    var topicId: String? = null
}
