package uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Term
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import java.time.LocalDate

fun generateTestSentence(
  dataSource: UpstreamApi = UpstreamApi.NOMIS,
  dateOfSentencing: LocalDate? = null,
  description: String? = "Some description",
  fineAmount: Number? = null,
  isActive: Boolean? = true,
  isCustodial: Boolean = true,
  terms: List<Term> = listOf(
    Term(hours = 2),
    Term(years = 25),
  ),
): Sentence = Sentence(
  dataSource = dataSource,
  dateOfSentencing = dateOfSentencing,
  description = description,
  fineAmount = fineAmount,
  isActive = isActive,
  isCustodial = isCustodial,
  terms = terms,
)
