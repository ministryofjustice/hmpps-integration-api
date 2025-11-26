package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.personSearchIdOnly
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val testRoleWithIdOnlyRedaction =
  role("test-role-id-only-redaction") {
    permissions {
      -fullAccess.permissions!!
    }
    redactionPolicies(
      listOf(
        personSearchIdOnly,
      ),
    )
  }
