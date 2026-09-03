package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val mojHmppsPersonLookup =
  role("moj-hmpps-person-lookup") {
    permissions {
      -"/v1/persons"
      -"/v1/status"
    }
  }

val mojHmppsPersonLookupV2 =
  role("moj-hmpps-person-lookup-v2") {
    permissions {
      -"/v2/persons"
      -"/v1/persons"
      -"/v1/status"
    }
  }
