package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence as IntegrationApiSentence

data class Sentence(
  val date: String? = null,
) {
    fun toSentence(): IntegrationApiSentence {
      return IntegrationApiSentence(
        dateOfSentencing = LocalDate.parse(this.date),
      )
    }
  }