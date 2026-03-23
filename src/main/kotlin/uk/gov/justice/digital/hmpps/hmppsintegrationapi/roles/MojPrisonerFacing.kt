package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val mojPrisonerFacing =
  role("moj-prisoner-facing") {
    permissions {
      -"/v1/persons/{hmppsId}/name"
      -"/v1/hmpps/id/by-nomis-number/{nomisNumber}"
      -"/v1/persons/{hmppsId}/cell-location"
      -"/v1/status"
    }
  }
