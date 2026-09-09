package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.policies.laoRedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.policies.riskScores.generalRiskScoreRedactions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.MappaCategory.CAT4
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val extHmppsPublicProtectionCat4 =
  role("ext-hmpps-public-protection-cat4") {
    permissions {
      -extHmppsPublicProtection.permissions!!
    }
    filters {
      mappaCategories {
        -CAT4
      }
    }
    redactionPolicies {
      -generalRiskScoreRedactions
      -laoRedactionPolicy
    }
  }
