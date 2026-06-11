package tr.erdaldemir.barem.domain.model

enum class WorkerType(val displayName: String, val enabledInDemo: Boolean) {
    MEMUR("Memur", enabledInDemo = true),
    ISCI("İşçi", enabledInDemo = false),
    EMEKLI("Emekli", enabledInDemo = false),
}
