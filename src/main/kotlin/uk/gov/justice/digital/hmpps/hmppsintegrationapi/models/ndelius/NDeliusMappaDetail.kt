package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.MappaDetail

data class NDeliusMappaDetail(
  val level: Int? = null,
  val levelDescription: String? = null,
  val category: Int? = null,
  val categoryDescription: String? = null,
  val startDate: String? = null,
  val reviewDate: String? = null,
  val notes: String? = null,
) {
  fun toMappaDetail(): MappaDetail = (
    MappaDetail(
      level = this.level,
      levelDescription = this.levelDescription,
      category = this.category,
      categoryDescription = this.categoryDescription,
      startDate = this.startDate,
      reviewDate = this.reviewDate,
      notes = this.notes,
    )
    )
}
