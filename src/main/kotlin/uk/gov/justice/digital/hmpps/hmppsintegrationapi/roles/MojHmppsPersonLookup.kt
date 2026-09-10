package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.policies.laoRedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val mojHmppsPersonLookup =
  role("moj-hmpps-person-lookup") {
    permissions {
      -"/v1/persons"
      -"/v1/status"
    }
    redactionPolicies {
      -laoRedactionPolicy
    }
  }

val mojHmppsPersonLookupV2 =
  role("moj-hmpps-person-lookup-v2") {
    permissions {
      -"/v2/persons"
      -"/v1/persons"
      -"/v1/persons/{hmppsId}"
      -"/v1/persons/{hmppsId}/addresses"
      -"/v1/persons/{hmppsId}/contacts"
      -"/v1/persons/{hmppsId}/offences"
      -"/v1/persons/{hmppsId}/sentences"
      -"/v1/contacts"
      -"/v1/contacts/{contactId}/linked-prisoners"
      -"/v1/addresses"
      -"/v1/status"
    }
    redactionPolicies {
      -laoRedactionPolicy
    }
  }
