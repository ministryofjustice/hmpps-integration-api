package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val testRoleWithPrisonFilters =
  role("test-role") {
    permissions {
      -fullAccess.permissions!!
    }
    filters {
      prisons {
        -"MDI"
        -"HEI"
      }
    }
  }
