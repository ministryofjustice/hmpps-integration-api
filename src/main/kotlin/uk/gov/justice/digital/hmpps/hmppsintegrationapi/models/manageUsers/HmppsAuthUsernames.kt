package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers

data class HmppsAuthUsernames(
  val azureAdUsername: String? = null,
  val nomisUsername: String? = null,
  val deliusUsername: String? = null,
) {
  fun hasUsername(source: HmppsAuthSource) =
    run {
      when (source) {
        HmppsAuthSource.AZUREAD -> azureAdUsername != null
        HmppsAuthSource.NDELIUS -> deliusUsername != null
        HmppsAuthSource.NOMIS -> nomisUsername != null
      }
    }
}
