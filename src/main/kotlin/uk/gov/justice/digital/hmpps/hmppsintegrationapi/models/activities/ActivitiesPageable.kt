package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

data class ActivitiesPageable(
  val offset: Int?,
  val sort: ActivitiesSort?,
  val pageSize: Int?,
  val paged: Boolean?,
  val pageNumber: Int?,
  val unpaged: Boolean?,
)
