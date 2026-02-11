package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val mojPrisonerFacing =
  role("moj-prisoner-facing") {
    permissions {
      -"/v1/persons/.*/name"
      -"/v1/hmpps/id/by-nomis-number/[^/]*$"
      -"/v1/persons/.*/cell-location"
      -"/v1/status"
    }
  }
