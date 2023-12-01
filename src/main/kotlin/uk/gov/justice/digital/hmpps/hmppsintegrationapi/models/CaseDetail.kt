package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

data class CaseDetail(
  val nomsId: String? = null,
  val name: Name? = null,
  val dateOfBirth: String? = null,
  val gender: String? = null,
  val sentence: CaseSentence? = null,
  val responsibleProvider: ResponsibleProvider? = null,
  val ogrsScore: Int? = null,
  val age: Int? = null,
  val ageAtRelease: Int? = null,
)

data class Name(
  val forename: String? = null,
  val middleName: String? = null,
  val surname: String? = null,
)

data class CaseSentence(
  val date: String? = null,
  val sentencingCourt: SentencingCourt? = null,
  val releaseDate: String? = null,
)

data class SentencingCourt(
  val name: String? = null,
)

data class ResponsibleProvider(
  val code: String? = null,
  val name: String? = null,
)
