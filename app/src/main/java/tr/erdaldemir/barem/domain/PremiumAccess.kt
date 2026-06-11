package tr.erdaldemir.barem.domain

import tr.erdaldemir.barem.domain.entitlement.EntitlementRepository

/**
 * Premium kapıları — [EntitlementRepository.isPremium] üzerinden.
 * UI: [tr.erdaldemir.barem.ui.premium.LocalEntitlement].
 */
object PremiumAccess {

    @Volatile
    private var repository: EntitlementRepository? = null

    fun bind(repo: EntitlementRepository) {
        repository = repo
    }

    fun isPremium(): Boolean = repository?.isPremium?.value == true

    fun repositoryOrNull(): EntitlementRepository? = repository
}
