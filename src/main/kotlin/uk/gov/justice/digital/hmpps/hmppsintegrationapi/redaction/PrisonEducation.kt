package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.MASK
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.REMOVE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.redactionPolicy

val prisonEducationRedactionPolicy =
  redactionPolicy("prison-education") {
    responseRedactions {
      jsonPath {
        type(MASK)
        includes {
          -"$..middleName"
          -"$.data.prisonerOffenderSearch.restrictionMessage"
          -"$.data.prisonerOffenderSearch.pncId"
          -"$.data.prisonerOffenderSearch.identifiers.croNumber"
          -"$.data.prisonerOffenderSearch.identifiers.deliusCrn"
        }
      }
      jsonPath {
        type(REMOVE)
        includes {
          -"$.data.probationOffenderSearch.contactDetails"
          -"$.data.probationOffenderSearch.currentRestriction"
          -"$.data.probationOffenderSearch.currentExclusion"
        }
      }
    }
  }
