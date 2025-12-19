package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.MASK
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.REMOVE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.redactionPolicy

val prisonEducationRedactionPolicy =
  redactionPolicy("prison-education") {
    responseRedactions {
      jsonPath {
        redactions {
          -("$..middleName" to MASK)
          -("$.data.prisonerOffenderSearch.restrictionMessage" to MASK)
          -("$.data.prisonerOffenderSearch.pncId" to MASK)
          -("$.data.prisonerOffenderSearch.identifiers.croNumber" to MASK)
          -("$.data.prisonerOffenderSearch.identifiers.deliusCrn" to MASK)
          -("$.data.probationOffenderSearch" to REMOVE)
        }
      }
    }
  }
