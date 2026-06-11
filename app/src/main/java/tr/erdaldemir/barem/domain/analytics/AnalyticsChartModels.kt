package tr.erdaldemir.barem.domain.analytics

data class YearPoint(
    val year: Int,
    val value: Double,
)

data class DualYearPoint(
    val year: Int,
    val primary: Double,
    val secondary: Double,
)

data class DeductionYearPoint(
    val year: Int,
    val netPct: Double,
    val primPct: Double,
    val gvPct: Double,
    val dvPct: Double,
)

data class AnalyticsChartBundle(
    val tlSeries: List<YearPoint>,
    val usdSeries: List<YearPoint>,
    val eurSeries: List<YearPoint>,
    val goldSeries: List<YearPoint>,
    val inflationVsSalary: List<DualYearPoint>,
    val deductionRates: List<DeductionYearPoint>,
    val activeYear: Int,
    val activeYearNet: Double?,
    val periodLabel: String?,
)
