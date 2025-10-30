package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.MASK
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.REMOVE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.redactionPolicy

val prisonEducationRedactionPolicy =
  redactionPolicy("prison-education") {
    responseRedactions {
      jsonPath {
        includes {
          path("$..middleName", MASK)
          path("$.data.prisonerOffenderSearch.restrictionMessage", MASK)
          path("$.data.prisonerOffenderSearch.pncId", MASK)
          path("$.data.prisonerOffenderSearch.identifiers.croNumber", MASK)
          path("$.data.prisonerOffenderSearch.identifiers.deliusCrn", MASK)
          path("$.data.probationOffenderSearch", REMOVE)
        }
      }
    }
  }
