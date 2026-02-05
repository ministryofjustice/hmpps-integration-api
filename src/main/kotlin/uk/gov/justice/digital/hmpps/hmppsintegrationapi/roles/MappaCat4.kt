package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.riskScores.generalRiskScoreRedactions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.MappaCategory.CAT4
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val mappaCat4 =
  role("mappa-cat4") {
    permissions {
      -mappa.permissions!!
    }
    filters {
      mappaCategories {
        -CAT4
      }
    }
    redactionPolicies(
      listOf(
        generalRiskScoreRedactions,
      ),
    )
  }
