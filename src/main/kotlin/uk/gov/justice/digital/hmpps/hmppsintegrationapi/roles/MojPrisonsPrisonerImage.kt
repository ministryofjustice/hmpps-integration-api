package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val mojPrisonsPrisonerImage =
  role("moj-prisons-prisoner-image") {
    permissions {
      -"/v1/persons/{hmppsId}/images"
      -"/v1/persons/{hmppsId}"
      -"/v1/persons/{hmppsId}/access-limitations"
      -"/v1/persons/{hmppsId}/images/{id}"
      -"/v1/persons/{hmppsId}/prisoner-base-location"
      -"/v1/prison/prisoners/{hmppsId}"
      -"/v1/status"
    }
  }
