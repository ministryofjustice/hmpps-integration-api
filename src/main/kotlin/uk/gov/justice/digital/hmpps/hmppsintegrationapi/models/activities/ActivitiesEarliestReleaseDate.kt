package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EarliestReleaseDate

data class ActivitiesEarliestReleaseDate(
  val releaseDate: String?,
  val isTariffDate: Boolean,
  val isIndeterminateSentence: Boolean,
  val isImmigrationDetainee: Boolean,
  val isConvictedUnsentenced: Boolean,
  val isRemand: Boolean,
) {
  fun toEarliestReleaseDate(): EarliestReleaseDate =
    EarliestReleaseDate(
      releaseDate = releaseDate,
      isTariffDate = isTariffDate,
      isIndeterminateSentence = isIndeterminateSentence,
      isImmigrationDetainee = isImmigrationDetainee,
      isConvictedUnsentenced = isConvictedUnsentenced,
      isRemand = isRemand,
    )
}
