package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.createAndVaryLicence

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Licence

data class CvlLicenceSummary(
  val id: String,
  val prisonNumber: String? = null,
  val statusCode: String? = null,
  val licenceType: String? = null,
  val createdDateTime: String? = null,
  val approvedDateTime: String? = null,
  val updatedDateTime: String? = null,
) {
  fun toLicence(): Licence =
    Licence(
      id = this.id,
      offenderNumber = this.prisonNumber,
      status = this.statusCode,
      typeCode = this.licenceType,
      createdDate = this.createdDateTime,
      approvedDate = this.approvedDateTime,
      updatedDate = this.updatedDateTime,
    )
}
