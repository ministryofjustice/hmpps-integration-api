package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class PunishmentCommentDto(
  val id: Number? = null,
  val comment: String? = null,
  val reasonForChange: String? = null,
  val createdByUserId: String? = null,
  val dateTime: String? = null,
)
