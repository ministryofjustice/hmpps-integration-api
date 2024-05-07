package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskManagementPlan
import java.util.stream.Collectors

data class CrnRiskManagementPlans(
  val crn: String,
  val limitedAccessOffender: String,
  val riskManagementPlan: List<CrnRiskManagementPlan>
) {
  fun toRiskManagementPlan(): List<RiskManagementPlan> {
    return this.riskManagementPlan.stream()
      .map {
        RiskManagementPlan(
          assessmentId = it.assessmentId,
          dateCompleted = it.dateCompleted,
          initiationDate = it.initiationDate,
          assessmentStatus = it.assessmentStatus,
          assessmentType = it.assessmentType,
          keyInformationCurrentSituation = it.keyInformationCurrentSituation,
          furtherConsiderationsCurrentSituation = it.furtherConsiderationsCurrentSituation,
          supervision = it.supervision,
          monitoringAndControl = it.monitoringAndControl,
          interventionsAndTreatment = it.interventionsAndTreatment,
          victimSafetyPlanning = it.victimSafetyPlanning,
          latestCompleteDate = it.latestCompleteDate,
          latestSignLockDate = it.latestSignLockDate,
        )
      }
      .collect(Collectors.toList())
  }
}

data class CrnRiskManagementPlan(
  val assessmentId: String,
  val dateCompleted: String,
  val partcompStatus: String,
  val initiationDate: String,
  val assessmentStatus: String,
  val assessmentType: String,
  val superStatus: String,
  val keyInformationCurrentSituation: String,
  val furtherConsiderationsCurrentSituation: String,
  val supervision: String,
  val monitoringAndControl: String,
  val interventionsAndTreatment: String,
  val victimSafetyPlanning: String,
  val contingencyPlans: String,
  val laterWIPAssessmentExists: String,
  val latestWIPDate: String,
  val laterSignLockAssessmentExists: String,
  val latestSignLockDate: String,
  val laterPartCompUnsignedAssessmentExists: String,
  val latestPartCompUnsignedDate: String,
  val laterPartCompSignedAssessmentExists: String,
  val latestPartCompSignedDate: String,
  val laterCompleteAssessmentExists: String,
  val latestCompleteDate: String,
)

