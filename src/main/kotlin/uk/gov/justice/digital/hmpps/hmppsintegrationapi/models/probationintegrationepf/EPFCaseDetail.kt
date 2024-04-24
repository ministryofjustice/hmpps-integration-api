package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationintegrationepf

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CourtAppearance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Name
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ResponsibleProvider

data class EPFCaseDetail(
  val nomsId: String? = null,
  val name: Name? = null,
  val dateOfBirth: String? = null,
  val gender: String? = null,
  val courtAppearance: CourtAppearance? = null,
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
      courtAppearance = this.courtAppearance,
      sentence =
        CaseSentence(
          expectedReleaseDate = sentence?.expectedReleaseDate,
        ),
      responsibleProvider = ResponsibleProvider(code = this.responsibleProvider?.code, name = this.responsibleProvider?.name),
      ogrsScore = this.ogrsScore,
      age = this.age,
      ageAtRelease = this.ageAtRelease,
    )
}
