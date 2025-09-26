package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonPayBand

data class ActivitiesPrisonPayBand(
  val id: Long,
  val displaySequence: Int,
  val alias: String,
  val description: String,
  val nomisPayBand: Int,
  val prisonCode: String,
  val createdTime: String?,
  val createdBy: String?,
  val updatedTime: String?,
  val updatedBy: String?,
) {
  fun toPrisonPayBand(): PrisonPayBand =
    PrisonPayBand(
      id = this.id,
      alias = this.alias,
      description = this.description,
    )
}
