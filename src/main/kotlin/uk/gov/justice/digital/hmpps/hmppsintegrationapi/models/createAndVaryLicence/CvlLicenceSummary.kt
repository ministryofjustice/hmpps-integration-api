package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.createAndVaryLicence

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Licence
data class CvlLicenceSummary(
  val id: String,
  val prisonNumber: String? = null,
) {
  fun toLicence(): Licence = Licence(
    id = this.id,
    offenderNumber = this.prisonNumber,
  )
}
