package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val hmppsSystem =
  role("hmpps-system") {
    permissions {
      -"/health/liveness"
      -"/health/readiness"
      -"/v1/status"
      -"/v2/config/authorisation"
    }
  }
