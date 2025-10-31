package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val mojPrisonerFacing =
  role("moj-prisoner-facing") {
    permissions {
      -"/v1/persons/.*/name"
      -"/v1/hmpps/id/by-nomis-number/[^/]*$"
      -"/v1/hmpps/id/nomis-number/by-hmpps-id/[^/]*$"
      -"/v1/persons/.*/cell-location"
      -"/v1/prison/prisoners/[^/]*$" // This has been provided for testing purposes. Approval needed before access can be provided in prod.
      -"/v1/status"
    }
  }
