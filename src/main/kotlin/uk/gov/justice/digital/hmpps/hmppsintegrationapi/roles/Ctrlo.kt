package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.riskScores.epfRiskScoreRedactions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val ctrlo =
  role("ctrlo") {
    permissions {
      -"/v1/epf/person-details/{hmppsId}/{eventNumber}"
      -"/v1/status"
      -"/v1/persons/{hmppsId}/access-limitations"
      -"/v1/persons/{hmppsId}/risks/scores"
    }
    redactionPolicies(
      listOf(
        epfRiskScoreRedactions,
      ),
    )
  }
