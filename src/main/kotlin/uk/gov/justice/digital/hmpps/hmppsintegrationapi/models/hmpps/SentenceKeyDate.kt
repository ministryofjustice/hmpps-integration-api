package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SentenceKeyDate(
  val date: LocalDate? = null,
  val overrideDate: LocalDate? = null,
  val calculatedDate: LocalDate? = null,
)
