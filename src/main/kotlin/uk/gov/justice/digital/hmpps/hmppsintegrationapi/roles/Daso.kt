package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val daso =
  role("daso") {
    permissions {
      -"/v1/status"
      -"/v1/persons/{hmppsId}/access-limitations"
    }
  }
