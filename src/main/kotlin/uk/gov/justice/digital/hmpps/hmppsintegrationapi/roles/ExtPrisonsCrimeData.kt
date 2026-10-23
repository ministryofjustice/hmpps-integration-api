package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val extPrisonsCrimeData =
  role("ext-prisons-crime-data") {
    permissions {
      -"/v1/prison/prisoners"
      -"/v1/prison/prisoners/{hmppsId}"
      -"/v1/persons/{hmppsId}/addresses"
      -"/v1/persons/{hmppsId}/sentences/latest-key-dates-and-adjustments"
      -"/v1/persons/{hmppsId}/offences"
      -"/v1/persons/{hmppsId}/prison-timeline"
      -"/v1/persons/{hmppsId}/protected-characteristics"
      -"/v1/hmpps/reference-data"
      -"/v1/status"
    }
    filters {
      supervisionStatuses {
        -"PRISONS"
      }
    }
  }
