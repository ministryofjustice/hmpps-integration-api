package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.tier

import java.math.BigDecimal

data class VersionedStaticOrDynamicPredictorDto(
  val algorithmVersion: String? = null,
  val staticOrDynamic: String? = null,
  val score: BigDecimal? = null,
  val band: String? = null,
)
