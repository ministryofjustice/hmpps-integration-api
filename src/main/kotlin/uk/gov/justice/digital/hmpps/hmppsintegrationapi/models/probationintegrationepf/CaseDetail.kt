package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationintegrationepf

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.CaseDetail as IntegrationAPICaseDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.CaseSentence as IntegrationAPICaseSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Name as IntegrationAPIName
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ResponsibleProvider as IntegrationAPIResponsibleProvider
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentencingCourt as IntegrationAPISentencingCourt

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
) {
  fun toCaseDetail(): IntegrationAPICaseDetail = IntegrationAPICaseDetail(
    nomsId = this.nomsId,
    name = IntegrationAPIName(forename = this.name?.forename, middleName = this.name?.middleName, surname = this.name?.surname),
    dateOfBirth = this.dateOfBirth,
    gender = this.gender,
    sentence = IntegrationAPICaseSentence(date = this.sentence?.date, sentencingCourt = IntegrationAPISentencingCourt(this.sentence?.sentencingCourt?.name), releaseDate = this.sentence?.releaseDate),
    responsibleProvider = IntegrationAPIResponsibleProvider(code = this.responsibleProvider?.code, name = this.responsibleProvider?.name),
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
