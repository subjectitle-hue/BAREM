package tr.erdaldemir.barem.domain.model

import androidx.annotation.StringRes
import tr.erdaldemir.barem.R

data class GorusTopic(
    val id: String,
    @StringRes val titleRes: Int,
)

data class GorusCategory(
    val id: String,
    @StringRes val titleRes: Int,
    val style: HomeTileStyle,
    val topics: List<GorusTopic> = emptyList(),
)

object GorusCatalog {
    private val memurTopics = listOf(
        GorusTopic("mali_haklar", R.string.gorus_topic_mali_haklar),
        GorusTopic("sosyal_haklar", R.string.gorus_topic_sosyal_haklar),
        GorusTopic("atama", R.string.gorus_topic_atama),
        GorusTopic("izin", R.string.gorus_topic_izin),
        GorusTopic("disiplin", R.string.gorus_topic_disiplin),
        GorusTopic("gorevde_yukselme", R.string.gorus_topic_gorevde_yukselme),
        GorusTopic("adaylik", R.string.gorus_topic_adaylik),
        GorusTopic("vekalet", R.string.gorus_topic_vekalet),
        GorusTopic("ek_ders", R.string.gorus_topic_ek_ders),
    )

    val categories: List<GorusCategory> = listOf(
        GorusCategory("memur", R.string.gorus_cat_memur, HomeTileStyle.Maas, memurTopics),
        GorusCategory("akademik", R.string.gorus_cat_akademik, HomeTileStyle.AsgariUcret),
        GorusCategory("sozlesmeli", R.string.gorus_cat_sozlesmeli, HomeTileStyle.Sozlesmeli),
        GorusCategory("surekli_isci", R.string.gorus_cat_surekli_isci, HomeTileStyle.KamuIscisi),
        GorusCategory("gecici_isci", R.string.gorus_cat_gecici_isci, HomeTileStyle.Harcirah),
        GorusCategory("diger", R.string.gorus_cat_diger, HomeTileStyle.DigerMaas),
    )

    fun category(id: String): GorusCategory? = categories.find { it.id == id }

    fun topic(categoryId: String, topicId: String): GorusTopic? =
        category(categoryId)?.topics?.find { it.id == topicId }
}
