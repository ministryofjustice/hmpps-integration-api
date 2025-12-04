package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val testRoleWithProbationOnlySupervisionFilters =
  role("test-role") {
    permissions {
      -fullAccess.permissions!!
    }
    filters {
      supervisionStatuses {
        -"PROBATION"
      }
    }
  }

val testRoleWithPrisonOnlySupervisionFilters =
  role("test-role") {
    permissions {
      -fullAccess.permissions!!
    }
    filters {
      supervisionStatuses {
        -"PRISONS"
      }
    }
  }

val testRoleWithPrisonAndProbationSupervisionFilters =
  role("test-role") {
    permissions {
      -fullAccess.permissions!!
    }
    filters {
      supervisionStatuses {
        -"PRISONS"
        -"PROBATION"
      }
    }
  }
