package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.remandAndSentencing

import java.time.LocalDate

data class RasSentencedCourtCases(
  val courtCases: List<RasSentencedCourtCase> = emptyList(),
)

data class RasSentencedCourtCase(
  val latestAppearance: RasCourtAppearance? = null,
  val appearances: List<RasCourtAppearance> = emptyList(),
)

data class RasCourtAppearance(
  val outcome: RasCourtAppearanceOutcome? = null,
  val appearanceDate: LocalDate? = null,
  val courtCode: String? = null,
  val charges: List<RasCharge> = emptyList(),
)

data class RasCourtAppearanceOutcome(
  val outcomeUuid: String? = null,
  val outcomeName: String? = null,
  val nomisCode: String? = null,
  val outcomeType: String? = null,
  val displayOrder: Int? = null,
  val relatedChargeOutcomeUuid: String? = null,
  val isSubList: Boolean? = null,
  val dispositionCode: String? = null,
  val status: String? = null,
  val warrantType: String? = null,
)

data class RasCharge(
  val sentence: RasSentence? = null,
)

data class RasSentence(
  val convictionDate: LocalDate? = null,
)
