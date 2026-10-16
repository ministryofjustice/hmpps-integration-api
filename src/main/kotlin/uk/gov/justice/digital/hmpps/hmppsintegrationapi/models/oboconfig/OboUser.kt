package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.oboconfig

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers.HmppsAuthUsernames

data class OboUser(
  val username: String,
  val hasPrisonRole: Boolean? = null,
  val authUsernames: HmppsAuthUsernames? = null,
)
