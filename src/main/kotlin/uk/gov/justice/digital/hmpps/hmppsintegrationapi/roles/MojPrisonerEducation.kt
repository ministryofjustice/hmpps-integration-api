package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val mojPrisonerEducation =
  role("moj-prisoner-education") {
    permissions {
      -"/v1/persons/.*/name"
      -"/v1/hmpps/id/nomis-number/[^/]*$"
      -"/v1/hmpps/id/by-nomis-number/[^/]*$"
      -"/v1/hmpps/id/nomis-number/by-hmpps-id/[^/]*$"
      -"/v1/persons/.*/cell-location"
      -"/v1/status"
    }
  }
