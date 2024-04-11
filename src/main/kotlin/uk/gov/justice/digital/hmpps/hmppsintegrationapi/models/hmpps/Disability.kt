package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import java.time.LocalDate

class Disability(
  val disabilityType: KeyValue? = null,
  val condition: KeyValue? = null,
  val startDate: LocalDate? = null,
  val endDate: LocalDate? = null,
  val notes: String? = null,
)
