package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.tier

import java.math.BigDecimal

data class BasePredictorDto(
  val score: BigDecimal? = null,
  val band: String? = null,
)
