package uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Sentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceLength
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceTerm
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SystemSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import java.time.LocalDate

fun generateTestSentence(
  serviceSource: UpstreamApi = UpstreamApi.NOMIS,
  systemSource: SystemSource = SystemSource.PRISON_SYSTEMS,
  dateOfSentencing: LocalDate? = null,
  description: String? = "Some description",
  fineAmount: Number? = null,
  isActive: Boolean? = true,
  isCustodial: Boolean = true,
  length: SentenceLength =
    SentenceLength(
      duration = null,
      units = null,
      terms =
        listOf(
          SentenceTerm(hours = 2),
          SentenceTerm(years = 25),
        ),
    ),
): Sentence =
  Sentence(
    serviceSource = serviceSource,
    systemSource = systemSource,
    dateOfSentencing = dateOfSentencing,
    description = description,
    fineAmount = fineAmount,
    isActive = isActive,
    isCustodial = isCustodial,
    length = length,
  )
