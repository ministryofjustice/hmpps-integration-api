package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val prisonerEscortCustodyServiceRole =
  role {
    include {
      -"/v1/status"
    }
  }
