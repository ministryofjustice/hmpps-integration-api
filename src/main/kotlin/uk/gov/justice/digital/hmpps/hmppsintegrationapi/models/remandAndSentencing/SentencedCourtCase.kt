package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.remandAndSentencing

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Appearance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CourtCasesSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CourtOutcome
import java.time.LocalDate

data class RasSentencedCourtCases(
  val courtCases: List<RasSentencedCourtCase> = emptyList(),
) {
  fun toCourtCasesSummary(): CourtCasesSummary {
    val allCourtAppearances =
      this.courtCases
        .flatMap { case ->
          (case.appearances + listOf(case.latestAppearance)).distinct()
        }.sortedByDescending { it?.appearanceDate }

    val dateOfFirstConviction =
      this.courtCases
        .flatMap { case ->
          (case.latestAppearance?.charges?.map { it } ?: emptyList()) + case.appearances.flatMap { it.charges }
        }.mapNotNull { it.sentence?.convictionDate }
        .minOrNull()

    val courtOutcome = allCourtAppearances.firstOrNull()?.outcome?.let { CourtOutcome(CourtOutComeType.from(it.outcomeType), it.outcomeName) }
    val courtCode = allCourtAppearances.firstOrNull { it?.outcome?.outcomeType == CourtOutComeType.SENTENCING.name }?.courtCode

    val allAppearances =
      allCourtAppearances.map { case ->
        Appearance(
          appearanceDate = case?.appearanceDate,
          courtCode = case?.courtCode,
          courtOutcome = case?.outcome?.let { CourtOutcome(CourtOutComeType.from(it.outcomeType), it.outcomeName) },
        )
      }

    return CourtCasesSummary(
      courtOutcome = courtOutcome,
      dateOfFirstConviction = dateOfFirstConviction,
      courtCode = courtCode,
      allAppearances = allAppearances,
    )
  }
}

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

enum class CourtOutComeType {
  SENTENCING,
  APPEAL,
  REMAND,
  OTHER,
  ;

  companion object {
    fun from(type: String?): CourtOutComeType = CourtOutComeType.entries.firstOrNull { it.name == type } ?: OTHER
  }
}
