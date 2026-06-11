package tr.erdaldemir.barem.domain.model

data class MKadroRow(
    val hizmetSinifi: String,
    val barem: Int?,
    val unvan: String,
    val detay: String,
    val derece: Int?,
) {
    val displayLabel: String
        get() = buildString {
            append(unvan)
            if (detay.isNotBlank()) append(" — ").append(detay)
            derece?.let { append(" (Derece ").append(it).append(')') }
        }
}
