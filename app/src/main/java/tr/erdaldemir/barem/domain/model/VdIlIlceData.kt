package tr.erdaldemir.barem.domain.model

data class VdIlIlceData(
    val iller: List<String>,
    val ilcelerByIl: Map<String, List<String>>,
    val bolgeselKodByKey: Map<String, String?>,
)
