package tr.erdaldemir.barem.domain.session

import tr.erdaldemir.barem.domain.model.MemurFormState

/** Kayıtlı hesap — yalnızca form + özet; yıllık seri açılışta yeniden hesaplanır. */
data class CalcHistoryRecord(
    val id: String,
    val savedAtEpochMs: Long,
    val form: MemurFormState,
    val kadroSummary: String,
    val netSummary: String,
    val periodLabel: String,
)
