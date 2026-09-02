package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.policies.prisonEducationRedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val testRoleEducationRedactionPolicy =
  role("test-role-education-redaction-policy") {
    permissions {
      -fullAccess.permissions!!
    }
    redactionPolicies(
      listOf(
        prisonEducationRedactionPolicy,
      ),
    )
  }
