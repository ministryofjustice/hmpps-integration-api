package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val extUp3CaseStatus =
  role("ext-up3-case-status") {
    permissions {
      -"/v1/cases/{caseId}/status"
      -"/v1/status"
    }
  }
