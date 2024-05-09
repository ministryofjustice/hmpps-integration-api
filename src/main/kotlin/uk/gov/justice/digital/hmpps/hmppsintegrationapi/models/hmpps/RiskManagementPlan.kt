package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class RiskManagementPlan(
  val assessmentId: String,
  val dateCompleted: String,
  val initiationDate: String,
  val assessmentStatus: String,
  val assessmentType: String,
  val keyInformationCurrentSituation: String,
  val furtherConsiderationsCurrentSituation: String,
  val supervision: String,
  val monitoringAndControl: String,
  val interventionsAndTreatment: String,
  val victimSafetyPlanning: String,
  val latestSignLockDate: String,
  val latestCompleteDate: String,
)
