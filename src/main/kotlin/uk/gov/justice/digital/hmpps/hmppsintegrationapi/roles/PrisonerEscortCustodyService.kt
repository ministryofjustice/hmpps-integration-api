package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val prisonerEscortCustodyService =
  role("prisoner-escort-custody-service") {
    permissions {
      -"/v1/status"
      -"/v1/persons/.*/case-notes"
      -"/va/persons/.*/languages"
    }
    filters {
      caseNotes {
        -"CAB"
        -"NEG"
        -"CVM"
        -"INTERVENTION"
        -"POS"
      }
    }
  }
