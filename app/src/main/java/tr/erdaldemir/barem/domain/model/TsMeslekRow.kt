package tr.erdaldemir.barem.domain.model

data class TsMeslekRow(
    val hizmetKolu: String,
    val nitelik: String,
    val kodPrefix: String,
    val hizmetSinifi: String,
    val sira: Int?,
    val kod: String,
    val detay: String,
) {
    val displayLabel: String
        get() = if (detay.isNotBlank()) "$kod — $detay" else kod
}
