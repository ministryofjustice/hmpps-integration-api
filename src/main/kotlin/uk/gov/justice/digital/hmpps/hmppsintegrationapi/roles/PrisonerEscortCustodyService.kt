package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val prisonerEscortCustodyService =
  role("prisoner-escort-custody-service") {
    permissions {
      -"/v1/status"
      -"/v1/persons/{hmppsId}/addresses"
      -"/v1/persons/{hmppsId}/case-notes"
      -"/v1/persons/{hmppsId}/emergency-contacts"
      -"/v1/persons/{hmppsId}/health-and-diet"
      -"/v1/persons/{hmppsId}/iep-level"
      -"/v1/persons/{hmppsId}/languages"
      -"/v1/persons/{hmppsId}/protected-characteristics"
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
