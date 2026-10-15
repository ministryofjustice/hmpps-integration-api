package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.tier

data class Registrations(
  val hasIomNominal: Boolean? = null,
  val hasLiferIpp: Boolean? = null,
  val hasDomesticAbuse: Boolean? = null,
  val hasStalking: Boolean? = null,
  val hasChildProtection: Boolean? = null,
  val complexityFactors: Collection<String>? = null,
  val rosh: String? = null,
  val mappaLevel: String? = null,
  val mappaCategory: String? = null,
  val unsupervised: Boolean? = null,
)
