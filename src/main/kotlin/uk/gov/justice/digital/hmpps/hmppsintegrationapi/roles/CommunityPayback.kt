package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val communityPayback =
  role("community-payback") {
    permissions {
      -"/v1/education/course-completion"
      -"/v1/status"
    }
  }
