package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

data class ActivitiesPagedWaitingListApplication(
  val totalPages: Int?,
  val totalElements: Int?,
  val first: Boolean?,
  val last: Boolean?,
  val size: Int?,
  val content: List<ActivitiesWaitingListApplication>?,
  val number: Int?,
  val sort: ActivitiesSort?,
  val numberOfElements: Int?,
  val pageable: ActivitiesPageable?,
  val empty: Boolean?,
)
