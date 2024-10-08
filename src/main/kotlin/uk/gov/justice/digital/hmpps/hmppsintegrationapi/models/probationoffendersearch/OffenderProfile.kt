package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Disability

data class OffenderProfile(
  val ethnicity: String? = null,
  val nationality: String? = null,
  val religion: String? = null,
  val sexualOrientation: String? = null,
  val disabilities: List<Disability> = emptyList(),
)
