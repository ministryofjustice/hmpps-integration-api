package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val prisonerEscortCustodyService =
  role("prisoner-escort-custody-service") {
    permissions {
      -"/v1/status"
      -"/v1/persons/.*/addresses"
      -"/v1/persons/.*/case-notes"
      -"/v1/persons/.*/emergency-contacts"
      -"/v1/persons/.*/health-and-diet"
      -"/v1/persons/.*/iep-level"
      -"/v1/persons/.*/languages"
      -"/v1/persons/.*/protected-characteristics"
    }
    filters {
      caseNotes {
        -"CAB"
        -"NEG"
        -"CVM"
        -"INTERVENTION"
        -"POS"
      }
      supervisionStatuses {
        -"PRISONS"
      }
    }
  }
