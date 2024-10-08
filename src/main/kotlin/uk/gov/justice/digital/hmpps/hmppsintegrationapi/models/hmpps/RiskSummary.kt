package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class RiskSummary(
  @Schema(description = "Who is at risk", example = "X, Y and Z are at risk")
  val whoIsAtRisk: String? = null,
  @Schema(description = "What is the nature of the risk", example = "The nature of the risk is X")
  val natureOfRisk: String? = null,
  @Schema(description = "When is the risk likely to be greatest. Consider the timescale and indicate whether risk is immediate or not. Consider the risks in custody as well as on release.", example = "The risk is imminent and more probable in X situation")
  val riskImminence: String? = null,
  @Schema(description = "What circumstances are likely to increase risk. Describe factors, actions, events which might increase level of risk, now and in the future.", example = "If offender in situation X the risk can be higher")
  val riskIncreaseFactors: String? = null,
  @Schema(description = "What factors are likely to reduce the risk. Describe factors, actions, and events which may reduce or contain the level of risk. What has previously stopped them?", example = "Giving offender therapy in X will reduce the risk")
  val riskMitigationFactors: String? = null,
  @Schema(description = "The overall risk level", example = "HIGH", allowableValues = ["VERY_HIGH", "HIGH", "MEDIUM", "LOW"])
  val overallRiskLevel: String? = null,
  @Schema(
    description = "Assess the risk of serious harm the offender poses on the basis that they could be released imminently back into the community. This field is a map which can return all or some of the properties given.",
    example = """
      {
        "children": "HIGH",
        "public": "MEDIUM",
        "knownAdult": "VERY_HIGH",
        "staff": "HIGH",
        "prisoners": "LOW"
      }
    """,
  )
  val riskInCommunity: Map<String, String>? = null,
  @Schema(
    description = "Assess both the risk of serious harm the offender presents now, in custody, and the risk they could present to others whilst in a custodial setting. This field is a map which can return all or some of the properties given.",
    example = """
      {
        "children": "HIGH",
        "public": "MEDIUM",
        "knownAdult": "VERY_HIGH",
        "staff": "HIGH",
        "prisoners": "LOW"
      }
    """,
  )
  val riskInCustody: Map<String, String>? = null,
)
