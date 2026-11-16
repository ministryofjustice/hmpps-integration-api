package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val mojHmppsAgencyData =
  role("moj-hmpps-agency-data") {
    permissions {
      -"/v1/hmpps/id/nomis-number/{nomisNumber}"
      -"/v1/persons/{hmppsId}"
      -"/v1/persons/{hmppsId}/access-limitations"
      -"/v1/persons/{hmppsId}/addresses"
      -"/v1/persons/{hmppsId}/images"
      -"/v1/persons/{hmppsId}/languages"
      -"/v1/persons/{hmppsId}/licences/conditions"
      -"/v1/persons/{hmppsId}/physical-characteristics"
      -"/v1/persons/{hmppsId}/protected-characteristics"
      -"/v1/persons/{hmppsId}/sentences"
      -"/v1/persons/{hmppsId}/sentences/latest-key-dates-and-adjustments"
      -"/v1/status"
    }
  }
