package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.interfaces

interface IPaginatedObject<T> {
  val content: List<T>
  val isLastPage: Boolean
  val count: Int
  val page: Int
  val perPage: Int
  val totalCount: Long
  val totalPages: Int
}
