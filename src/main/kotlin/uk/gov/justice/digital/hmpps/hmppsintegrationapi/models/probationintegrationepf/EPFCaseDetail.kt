package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationintegrationepf

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Name
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ResponsibleProvider
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentencingCourt

data class EPFCaseDetail(
  val nomsId: String? = null,
  val name: Name? = null,
  val dateOfBirth: String? = null,
  val gender: String? = null,
  val sentence: CaseSentence? = null,
  val responsibleProvider: ResponsibleProvider? = null,
  val ogrsScore: Long? = null,
  val age: Long? = null,
  val ageAtRelease: Long? = null,
) {
  fun toCaseDetail(): CaseDetail =
    CaseDetail(
      nomsId = this.nomsId,
      name = Name(forename = this.name?.forename, middleName = this.name?.middleName, surname = this.name?.surname),
      dateOfBirth = this.dateOfBirth,
      gender = this.gender,
      sentence =
        CaseSentence(
          sentenceDate = this.sentence?.sentenceDate,
          sentencingCourt = SentencingCourt(this.sentence?.sentencingCourt?.name),
          releaseDate = this.sentence?.releaseDate,
        ),
      responsibleProvider = ResponsibleProvider(code = this.responsibleProvider?.code, name = this.responsibleProvider?.name),
      ogrsScore = this.ogrsScore,
      age = this.age,
      ageAtRelease = this.ageAtRelease,
    )
}

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
