package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.personSearchIdOnly
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val smartinbox =
  role("smartinbox") {
    permissions {
      -"/v1/status"
      -"/v1/persons"
      -"/v1/persons/[^/]+/prisoner-base-location"
    }
    redactionPolicies(
      listOf(
        personSearchIdOnly,
      ),
    )
  }
