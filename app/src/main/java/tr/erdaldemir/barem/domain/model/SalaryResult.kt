package tr.erdaldemir.barem.domain.model

data class SalaryResult(
    val brutAylik: Double,
    val kesintiler: Double,
    val netAylik: Double,
    val cocukYardimi: Double,
    val toplamNet: Double,
    val yillikBrut: Double,
    val yillikNet: Double,
    val altinGramKarsilik: Double,
    val dolarKarsilik: Double,
    /** OCAK–HAZİRAN ort. (A!16). */
    val firstHalfAvg: Double = 0.0,
    /** TEMMUZ–ARALIK ort. (A!17). */
    val secondHalfAvg: Double = 0.0,
    val periodLabel: String,
    val lineItems: List<String> = emptyList(),
    /** OCAK…ARALIK aylık net (H!BN166–177). */
    val monthlyNet: List<Double> = emptyList(),
    val monthlyLabels: List<String> = MONTH_LABELS,
    /** A!22–54 — USD / EUR / çeyrek altın aylık ve dönem ortalamaları. */
    val fxOutput: FxOutput? = null,
    val deductionBreakdown: DeductionBreakdown? = null,
) {
    companion object {
        val MONTH_LABELS = listOf(
            "OCAK", "ŞUBAT", "MART", "NİSAN", "MAYIS", "HAZİRAN",
            "TEMMUZ", "AĞUSTOS", "EYLÜL", "EKİM", "KASIM", "ARALIK",
        )
    }
}
