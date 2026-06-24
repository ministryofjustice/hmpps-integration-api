package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val personLookup =
  role("person-lookup") {
    permissions {
      -"/v1/addresses"
      -"/v1/contacts"
      -"/v1/contacts/{contactId}/linked-prisoners"
      -"/v1/persons"
      -"/v1/persons/{hmppsId}"
      -"/v1/persons/{hmppsId}/addresses"
      -"/v1/persons/{hmppsId}/contacts"
      -"/v1/persons/{hmppsId}/offences"
      -"/v1/persons/{hmppsId}/sentences"
      -"/v1/status"
    }
  }
