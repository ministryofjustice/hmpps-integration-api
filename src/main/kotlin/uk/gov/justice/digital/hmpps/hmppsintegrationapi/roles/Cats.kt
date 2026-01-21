package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val cats =
  role("cats") {
    permissions {
      -"/v1/persons/.*/risks/serious-harm"
      -"/v1/status"
    }
  }
