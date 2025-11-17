package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.laoRedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val testRoleWithLaoRedactions =
  role("test-role") {
    permissions {
      -fullAccess.permissions!!
    }
    redactionPolicies(
      listOf(
        laoRedactionPolicy,
      ),
    )
    filters {
      statusCodes {
        -testRoleWithPndAlerts.filters?.statusCodes!!
      }
    }
  }
