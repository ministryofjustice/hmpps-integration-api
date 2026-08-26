package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val extPrisonsCourseCompletion =
  role("ext-prisons-course-completion") {
    permissions {
      -"/v1/education/course-completion"
      -"/v1/status"
    }
  }
