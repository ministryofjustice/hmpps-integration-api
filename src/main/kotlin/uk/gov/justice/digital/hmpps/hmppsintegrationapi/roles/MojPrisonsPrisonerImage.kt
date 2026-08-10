package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val mojPrisonsPrisonerImage =
  role("moj-prisons-prisoner-image") {
    permissions {
      -"/v1/persons/{hmppsId}/images"
      -"/v1/persons/{hmppsId}/images/{id}"
      -"/v1/status"
    }
  }
