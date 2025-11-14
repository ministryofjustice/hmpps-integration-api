package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val testRoleWithPndAlerts =
  role("test-role-pnd") {
    permissions {
      -fullAccess.permissions!!
    }
    filters {
      alertCodes {
        -police.filters?.alertCodes!!
      }
    }
  }
