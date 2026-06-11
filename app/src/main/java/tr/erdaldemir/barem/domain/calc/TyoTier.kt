package tr.erdaldemir.barem.domain.calc

/**
 * H!CM127 — TYO göstergesi; CM69 (ek gösterge puanı) kademeleri.
 * Excel: CN22=8400→255 … CN27=2200→85, aksi CN35=55.
 */
object TyoTier {

    fun indicator(ekGostergePuan: Double): Double = when {
        ekGostergePuan <= 0 -> 55.0
        ekGostergePuan >= 8400 -> 255.0
        ekGostergePuan >= 7600 -> 215.0
        ekGostergePuan >= 6400 -> 195.0
        ekGostergePuan >= 4800 -> 165.0
        ekGostergePuan >= 3600 -> 145.0
        ekGostergePuan >= 2200 -> 85.0
        else -> 55.0
    }
}
