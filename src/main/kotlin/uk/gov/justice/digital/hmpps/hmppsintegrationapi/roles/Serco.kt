package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val serco =
  role("serco") {
    permissions {
      -"/v1/persons"
      -"/v1/prison/prisoners/{hmppsId}"
      -"/v1/prison/prisoners"
      -"/v1/persons/{hmppsId}/sentences/latest-key-dates-and-adjustments"
      -"/v1/hmpps/id/by-nomis-number/{nomisNumber}"
      -"/v1/status"
    }
  }
