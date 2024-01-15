package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.adjudications

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Adjudication

data class ReportedAdjudications(
  val adjudications: List<Adjudication>,
)
