package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val statusOnly =
  role("status-only") {
    permissions {
      -"/v1/status"
    }
  }
