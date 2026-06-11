package tr.erdaldemir.barem.domain.model

/** A!22–54 — aylık net / kur (USD, EUR, çeyrek altın). */
data class FxOutput(
    val monthlyUsd: List<Double>,
    val monthlyEur: List<Double>,
    val monthlyGoldQuarter: List<Double>,
    val firstHalfUsd: Double,
    val secondHalfUsd: Double,
    val yearlyAvgUsd: Double,
    val firstHalfEur: Double,
    val secondHalfEur: Double,
    val yearlyAvgEur: Double,
    val firstHalfGoldQuarter: Double,
    val secondHalfGoldQuarter: Double,
    val yearlyAvgGoldQuarter: Double,
)
