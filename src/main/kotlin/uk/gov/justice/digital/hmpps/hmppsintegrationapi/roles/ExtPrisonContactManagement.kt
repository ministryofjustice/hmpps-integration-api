package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val extPrisonContactManagement =
  role("ext-prison-contact-management") {
    permissions {
      -"/v1/contacts"
      -"/v1/persons/{hmppsId}/contacts"
      -"/v1/visit"
      -"/v1/visit/{visitReference}"
      -"/v1/visit/{visitReference}/cancel"
      -"/v1/status"
    }
    filters {
      supervisionStatuses {
        -"PRISONS"
      }
    }
  }
