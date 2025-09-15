package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val curiousRole =
  role {
    include {
      -"/v1/persons/.*/plp-induction-schedule"
      -"/v1/persons/.*/plp-induction-schedule/history"
      -"/v1/persons/.*/plp-review-schedule"
      -"/v1/persons/[^/]+/expression-of-interest/jobs/[^/]+$"
      -"/v1/hmpps/id/by-nomis-number/[^/]*$"
      -"/v1/hmpps/id/nomis-number/by-hmpps-id/[^/]*$"
      -"/v1/persons/.*/education/assessments/status"
      -"/v1/persons/[^/]*$"
      -"/v1/persons/[^/]+/prisoner-base-location"
      -"/v1/persons/.*/education/assessments"
      -"/v1/status"
      -"/v1/persons/.*/education/san/plan-creation-schedule"
      -"/v1/persons/.*/education/san/review-schedule"
      -"/v1/persons/.*/education/status"
      -"/v1/persons/.*/education/aln-assessment"
    }
  }
