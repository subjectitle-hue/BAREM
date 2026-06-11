package tr.erdaldemir.barem.domain.model

data class AFormOptions(
    val medeniHal: List<String> = emptyList(),
    val topluSozlesme: List<String> = emptyList(),
    val yabanciDil: List<String> = emptyList(),
)
