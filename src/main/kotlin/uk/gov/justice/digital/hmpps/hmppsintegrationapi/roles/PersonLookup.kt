package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val personLookup =
  role("person-lookup") {
    permissions {
      -"/v1/persons"
      -"/v1/status"
    }
  }
