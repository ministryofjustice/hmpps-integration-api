package uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence
import java.time.LocalDate

fun generateTestSentence(
  dateOfSentencing: LocalDate? = LocalDate.parse("2020-02-03"),
): Sentence = Sentence(
  dateOfSentencing = dateOfSentencing,
)
