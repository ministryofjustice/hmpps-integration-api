package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.policies.laoRedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val extHmppsSeriousHarm =
  role("ext-hmpps-serious-harm") {
    permissions {
      -"/v1/persons/{hmppsId}/risks/serious-harm"
      -"/v1/status"
    }
    redactionPolicies {
      -laoRedactionPolicy
    }
  }
