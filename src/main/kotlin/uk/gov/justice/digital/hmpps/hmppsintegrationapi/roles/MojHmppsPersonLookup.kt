package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val mojHmppsPersonLookup =
  role("moj-hmpps-person-lookup") {
    permissions {
      -"/v1/persons"
      -"/v1/persons/{hmppsId}"
      -"/v1/status"
    }
  }
