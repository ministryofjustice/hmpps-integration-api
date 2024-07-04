package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate

data class SentenceKeyDate(
  val date: LocalDate? = null,
  val overrideDate: LocalDate? = null,
  @JsonInclude(JsonInclude.Include.NON_NULL)
  val calculatedDate: LocalDate? = null,
)
