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
          -("$.data.adjustments" to REMOVE)
          -("$.data.earlyRemovalSchemeEligibilityDate" to REMOVE)
          -("$.data.earlyTerm" to REMOVE)
          -("$.data.homeDetentionCurfew.eligibilityCalculatedDate" to REMOVE)
          -("$.data.homeDetentionCurfew.eligibilityDate" to REMOVE)
          -("$.data.homeDetentionCurfew.eligibilityOverrideDate" to REMOVE)
          -("$.data.lateTerm" to REMOVE)
          -("$.data.midTerm" to REMOVE)
          -("$.data.nonDto" to REMOVE)
          -("$.data.nonParole" to REMOVE)
          -("$.data.paroleEligibility" to REMOVE)
          -("$.data.dtoPostRecallRelease" to REMOVE)
          -("$.data.tariffEarlyRemovalSchemeEligibilityDate" to REMOVE)
          -("$.data.topupSupervision" to REMOVE)
        }
      }
    }
  }
