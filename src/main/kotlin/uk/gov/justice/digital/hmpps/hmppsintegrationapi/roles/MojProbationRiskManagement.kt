package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val mojProbationRiskManagement =
  role("moj-probation-risk-management") {
    permissions {
      -"/v1/status"
      -"/v1/persons/{hmppsId}/access-limitations"
      -"/v1/persons/{hmppsId}/person-responsible-officer"
      -"/v1/persons/{hmppsID}/protected-characteristics"
      -"/v1/persons/{hmppsId}/risks/mappadetail"
      -"/v1/persons/{hmppsId}/risks/serious-harm"
      -"/v1/persons/{hmppsId}"
      -"/v1/persons/{hmppsId}/offences"
      -"/v1/persons/{hmppsId}/sentences/latest-key-dates-and-adjustments"
      -"/v1/persons/addresses/{hmppsID}"
    }
    filters {
      supervisionStatuses {
        -"PROBATION"
      }
    }
  }
