package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDateTime

data class RiskPredictorScore(
  val completedDate: LocalDateTime? = null,
  val assessmentStatus: String? = null,
  val generalPredictor: GeneralPredictor = GeneralPredictor(),
  val violencePredictor: ViolencePredictor = ViolencePredictor(),
  val groupReconviction: GroupReconviction = GroupReconviction(),
)
