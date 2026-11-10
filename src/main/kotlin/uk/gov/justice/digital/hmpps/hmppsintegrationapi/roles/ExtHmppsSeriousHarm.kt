package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.policies.laoRedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val extHmppsSeriousHarm =
  role("ext-hmpps-serious-harm") {
    permissions {
      -"/v1/persons/{hmppsId}/licences/conditions"
      -"/v1/persons/{hmppsId}/offences"
      -"/v1/persons/{hmppsId}/person-responsible-officer"
      -"/v1/persons/{hmppsId}/risks/serious-harm"
      -"/v1/persons/{hmppsId}/supervision-status"
      -"/v1/persons/{hmppsId}/sentences"
      -"/v1/persons/{hmppsId}/sentences/latest-key-dates-and-adjustments"
      -"/v1/persons/{hmppsId}/tier/details"
      -"/v1/status"
      -"/v2/persons/{hmppsId}"
    }
    redactionPolicies {
      -laoRedactionPolicy
    }
  }
