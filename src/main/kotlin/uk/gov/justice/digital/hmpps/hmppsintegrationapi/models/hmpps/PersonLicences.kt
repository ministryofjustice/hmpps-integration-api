package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class PersonLicences(
  val hmppsId: String,
  val offenderNumber: String? = null,
  val licences: List<Licence> = emptyList(),
)
