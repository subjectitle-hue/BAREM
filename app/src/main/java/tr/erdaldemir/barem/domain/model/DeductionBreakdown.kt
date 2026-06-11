package tr.erdaldemir.barem.domain.model

/** Yıllık ort. brüt üzerinden kesinti oranları (G grafik #6). */
data class DeductionBreakdown(
    val avgBrut: Double,
    val avgPrim: Double,
    val avgGv: Double,
    val avgDv: Double,
    val avgNet: Double,
) {
    val primPct: Double get() = pct(avgPrim)
    val gvPct: Double get() = pct(avgGv)
    val dvPct: Double get() = pct(avgDv)
    val netPct: Double get() = pct(avgNet)

    private fun pct(part: Double): Double =
        if (avgBrut > 0) kotlin.math.round(part / avgBrut * 1000.0) / 10.0 else 0.0
}
