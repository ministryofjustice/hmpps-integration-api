package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val mojProbationRiskManagement =
  role("moj-probation-risk-management") {
    permissions {
      -"/v1/status"
      -"/v1/persons/{hmppsId}/access-limitations"
      -"/v1/persons/{hmppsId}/risks/serious-harm"
      -"/v1/persons/{hmppsId}"
      -"/v1/persons/{hmppsId}/offences"
    }
    filters {
      supervisionStatuses {
        -"PROBATION"
      }
    }
  }
