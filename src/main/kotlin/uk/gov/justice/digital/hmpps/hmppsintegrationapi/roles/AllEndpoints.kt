package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.mapping.roleConstants

val allEndpoints =
  role("all-endpoints") {
    include {
      -roleConstants.include
    }
  }
