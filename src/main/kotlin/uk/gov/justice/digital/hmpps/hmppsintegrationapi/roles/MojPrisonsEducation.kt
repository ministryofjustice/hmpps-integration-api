package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.policies.laoRedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val mojPrisonsEducation =
  role("moj-prisons-education") {
    permissions {
      -"/v1/persons/{hmppsId}/name"
      -"/v1/hmpps/id/nomis-number/{nomisNumber}"
      -"/v1/hmpps/id/by-nomis-number/{nomisNumber}"
      -"/v1/hmpps/id/nomis-number/by-hmpps-id/{hmppsId}"
      -"/v1/persons/{hmppsId}/cell-location"
      -"/v1/status"
    }
    redactionPolicies {
      -laoRedactionPolicy
    }
  }
