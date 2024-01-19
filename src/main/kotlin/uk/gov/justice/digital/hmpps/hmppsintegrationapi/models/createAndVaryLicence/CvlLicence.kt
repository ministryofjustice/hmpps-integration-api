package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.createAndVaryLicence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Licence
data class CvlLicence(
  val prisonNumber: String? = null,
) {
  fun toLicence(): Licence = Licence(
    offenderNumber = this.prisonNumber,
  )
}
