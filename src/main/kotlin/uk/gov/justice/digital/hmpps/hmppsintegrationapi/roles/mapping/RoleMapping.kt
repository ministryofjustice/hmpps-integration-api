package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.mapping

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.curiousRole
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.fullAccessRole
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.mappaRole
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.policeRole
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.prisonerEscortCustodyServiceRole
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.privatePrisonRole
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.referenceDataOnly

val roles =
  mapOf(
    "private-prison" to privatePrisonRole,
    "police" to policeRole,
    "curious" to curiousRole,
    "reference-data-only" to referenceDataOnly,
    "full-access" to fullAccessRole,
    "prisoner-escort-custody-service-role" to prisonerEscortCustodyServiceRole,
    "mappa-role" to mappaRole,
  )
