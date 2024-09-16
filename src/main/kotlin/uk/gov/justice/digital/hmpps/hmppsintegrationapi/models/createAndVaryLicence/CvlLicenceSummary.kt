package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.createAndVaryLicence

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Licence

data class CvlLicenceSummary(
  val licenceId: String,
  val nomisId: String? = null,
  val licenceStatus: String? = null,
  val licenceType: String? = null,
  val dateCreated: String? = null,
  val approvedDate: String? = null,
) {
  fun toLicence(): Licence =
    Licence(
      id = this.licenceId,
      offenderNumber = this.nomisId,
      status = this.licenceStatus,
      typeCode = this.licenceType,
      createdDate = this.dateCreated,
      approvedDate = this.approvedDate,
    )
}
