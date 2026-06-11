package tr.erdaldemir.barem.domain.model

/** Excel V tablosu — dereceye göre geçerli kademe üst sınırı. */
object KademeRules {
    fun maxKademe(derece: Int): Int = when (derece) {
        1 -> 4
        2 -> 6
        3 -> 8
        else -> 9
    }

    fun kademeler(derece: Int): List<Int> =
        (1..maxKademe(derece)).toList()
}
