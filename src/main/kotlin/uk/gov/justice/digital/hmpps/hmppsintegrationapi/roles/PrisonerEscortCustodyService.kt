package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val prisonerEscortCustodyService =
  role("prisoner-escort-custody-service") {
    include {
      -"/v1/status"
      -"/v1/persons/.*/case-notes"
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
