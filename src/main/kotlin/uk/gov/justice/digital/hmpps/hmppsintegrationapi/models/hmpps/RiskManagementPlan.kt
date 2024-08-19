package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class RiskManagementPlan(
  @Schema(description = "The unique ID of the risk management plan", example = "123456")
  val assessmentId: String,
  @Schema(description = "The date that the risk management plan was completed", example = "2024-05-04T01:04:20")
  val dateCompleted: String,
  @Schema(description = "The date of plan initiation", example = "2024-05-04T01:04:20")
  val initiationDate: String,
  @Schema(description = "The status of the plan", example = "COMPLETE")
  val assessmentStatus: String,
  @Schema(description = "The type of assessment")
  val assessmentType: String,
  @Schema(description = "Key information about the current situation of the subject being assessed")
  val keyInformationCurrentSituation: String,
  @Schema(description = "Further considerations about the situation of the subject being assessed")
  val furtherConsiderationsCurrentSituation: String,
  @Schema(description = "Who they see, when and why, any support they get from their community, and how well they're desisting from problematic behaviour")
  val supervision: String,
  @Schema(description = "Information on restrictions in place to prevent reoffending, what steps have been taken to monitor potential reoffending, including license conditions, community order requirements, PPM restrictions and such.")
  val monitoringAndControl: String,
  @Schema(description = "Interventions delivered to develop controls and protective factors to reduce risk of reoffending, including practical support, requirements to support interventions and details of who and where these interventions will be administered.")
  val interventionsAndTreatment: String,
  @Schema(description = "Restrictions in place to specifically protect victims of, adults known to, and children potentially at risk from the offender.")
  val victimSafetyPlanning: String,
  @Schema(description = "Future plans in the form \"If X happens, we will do Y....\" for if parts of the risk management plan break down or requirements or restrictions are breached by the offender.")
  val contingencyPlans: String,
  @Schema(description = "An assessment is considered 'Signed and locked' once it is signed by the assessor, making the plan read-only. This is the date the plan has been signed by the assessor.", example = "2024-05-04T01:04:20")
  val latestSignLockDate: String,
  @Schema(description = "Once a countersignature has been applied to the plan, the plan is considered complete. This is the date the plan has been countersigned.", example = "2024-05-04T01:04:20")
  val latestCompleteDate: String,
)
