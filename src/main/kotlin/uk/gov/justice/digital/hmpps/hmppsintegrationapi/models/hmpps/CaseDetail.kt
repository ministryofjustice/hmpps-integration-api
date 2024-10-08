package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class CaseDetail(
  val nomsId: String? = null,
  val name: Name? = null,
  val dateOfBirth: String? = null,
  val gender: String? = null,
  val courtAppearance: CourtAppearance? = null,
  val sentence: CaseSentence? = null,
  val responsibleProvider: ResponsibleProvider? = null,
  val ogrsScore: Long? = null,
  val rsrScore: Double? = null,
  val age: Long? = null,
  val ageAtRelease: Long? = null,
)

data class Name(
  val forename: String? = null,
  val middleName: String? = null,
  val surname: String? = null,
)

data class CaseSentence(
  @Deprecated("This field is depreciated and will be removed from the endpoint /v1/epf/person-details/{hmppsId}/{eventNumber} response soon.")
  val date: String? = null,
  @Deprecated("This field is depreciated and will be removed from the endpoint /v1/epf/person-details/{hmppsId}/{eventNumber} response soon.")
  val sentencingCourt: SentencingCourt? = null,
  @Deprecated("This field is depreciated and will be removed from the endpoint /v1/epf/person-details/{hmppsId}/{eventNumber} response soon.")
  val releaseDate: String? = null,
  val expectedReleaseDate: String? = null,
)

data class SentencingCourt(
  val name: String? = null,
)

data class ResponsibleProvider(
  val code: String? = null,
  val name: String? = null,
)

data class CourtAppearance(
  val date: String? = null,
  val court: CourtDetails? = null,
)

data class CourtDetails(
  @Schema(description = "The name of the court", example = "Manchester Crown Court")
  val name: String? = null,
)
