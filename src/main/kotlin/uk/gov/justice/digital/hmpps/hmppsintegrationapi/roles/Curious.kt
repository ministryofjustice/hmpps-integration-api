package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.prisonEducationRedactionPolicy as prisonEducation

val curious =
  role("curious") {
    permissions {
      -"/v1/persons/{hmppsId}/plp-induction-schedule"
      -"/v1/persons/{hmppsId}/plp-induction-schedule/history"
      -"/v1/persons/{hmppsId}/plp-review-schedule"
      -"/v1/hmpps/id/by-nomis-number/{nomisNumber}"
      -"/v1/hmpps/id/nomis-number/by-hmpps-id/{hmppsId}"
      -"/v1/persons/{hmppsId}/education/assessments/status"
      -"/v1/persons/{hmppsId}"
      -"/v1/persons/{hmppsId}/prisoner-base-location"
      -"/v1/persons/{hmppsId}/education/assessments"
      -"/v1/status"
      -"/v1/persons/{hmppsId}/education/san/plan-creation-schedule"
      -"/v1/persons/{hmppsId}/education/san/review-schedule"
      -"/v1/persons/{hmppsId}/education/status"
      -"/v1/persons/{hmppsId}/education/aln-assessment"
    }
    redactionPolicies(
      listOf(
        prisonEducation,
      ),
    )
  }
