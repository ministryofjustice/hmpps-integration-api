package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val smartinbox =
  role("smartinbox") {
    permissions {
      -"/v1/status"
      -"/v1/persons/[^/]+/prisoner-base-location"
      -"/v1/hmpps/id/nomis-number/by-hmpps-id/[^/]*$"
    }
  }
