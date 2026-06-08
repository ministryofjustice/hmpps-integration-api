package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.REMOVE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.redactionPolicy

val unusedKeyDatesRedactions =
  redactionPolicy(
    "unused-key-dates-redactions",
  ) {
    responseRedactions {
      jsonPath {
        endpoints {
          -"/v1/persons/{hmppsId}/sentences/latest-key-dates-and-adjustments"
        }
        redactions {
          -("$..adjustments" to REMOVE)
          -("$..earlyRemovalSchemeEligibilityDate" to REMOVE)
          -("$..earlyTerm" to REMOVE)
          -("$.data.homeDetentionCurfew.eligibilityCalculatedDate" to REMOVE)
          -("$.data.homeDetentionCurfew.eligibilityDate" to REMOVE)
          -("$.data.homeDetentionCurfew.eligibilityOverrideDate" to REMOVE)
          -("$..lateTerm" to REMOVE)
          -("$..midTerm" to REMOVE)
          -("$..nonDto" to REMOVE)
          -("$..nonParole" to REMOVE)
          -("$..paroleEligibility" to REMOVE)
          -("$..postRecallRelease" to REMOVE)
          -("$.data.release.date" to REMOVE)
          -("$..postRecallRelease" to REMOVE)
          -("$..tariffEarlyRemovalSchemeEligibilityDate" to REMOVE)
          -("$..topupSupervision" to REMOVE)
        }
      }
    }
  }
