package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roleConstants
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.laoRedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.riskScores.generalRiskScoreRedactions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val fullAccessLaoRedactions =
  role("full-access-lao-redactions") {
    permissions {
      -roleConstants.allEndpoints
    }
    filters {
      prisons {
        -"*"
      }
      caseNotes {
        -"*"
      }
    }
    redactionPolicies(
      listOf(
        laoRedactionPolicy,
        generalRiskScoreRedactions,
      ),
    )
  }
