package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val referenceDataOnly = role {
  include {
    - "/v1/hmpps/reference-data"
    - "/v1/status"
  }
}

