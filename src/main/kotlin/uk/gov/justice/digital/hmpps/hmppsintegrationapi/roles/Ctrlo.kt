package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val ctrlo =
  role("ctrlo") {
    include {
      -"/v1/epf/person-details/.*/[^/]*$"
      -"/v1/status"
      -"/v1/persons/.*/access-limitations"
    }
  }
