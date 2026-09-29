package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val extCaseStatusUpdate =
  role("ext-case-status-update") {
    permissions {
      -"/v1/cases/{caseId}/status"
      -"/v1/status"
    }
  }
