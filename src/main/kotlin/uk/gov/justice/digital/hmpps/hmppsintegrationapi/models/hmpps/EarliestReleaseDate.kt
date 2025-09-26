package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class EarliestReleaseDate(
  val releaseDate: String?,
  val isTariffDate: Boolean,
  val isIndeterminateSentence: Boolean,
  val isImmigrationDetainee: Boolean,
  val isConvictedUnsentenced: Boolean,
  val isRemand: Boolean,
)
