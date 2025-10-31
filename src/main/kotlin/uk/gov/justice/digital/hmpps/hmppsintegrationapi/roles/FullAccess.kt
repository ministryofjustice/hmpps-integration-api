package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roleConstants
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val fullAccess =
  role("full-access") {
    permissions {
      -roleConstants.allEndpoints
    }
    filters {
      prisons {
        -"*"
      }
      caseNotes {
        -"*"
      }
    }
  }
