package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val testRoleWithPoliceIntelligenceAlerts =
  role("test-police-intelligence") {
    permissions {
      -fullAccess.permissions!!
    }
    filters {
      alertCodes {
        -extProbationPoliceIntelligence.filters?.alertCodes!!
      }
    }
  }
