package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityCategory

data class ActivitiesActivityCategory(
  val id: Long,
  val code: String,
  val name: String,
  val description: String?,
) {
  fun toActivityCategory() =
    ActivityCategory(
      code = this.code,
      name = this.name,
      description = this.description,
    )
}
