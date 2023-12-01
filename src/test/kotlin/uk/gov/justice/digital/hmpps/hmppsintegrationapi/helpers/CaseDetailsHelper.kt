package uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.CaseDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.CaseSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Name
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ResponsibleProvider
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentencingCourt

fun generateCaseDetail(): CaseDetail {
  return CaseDetail(
    nomsId = "ABC123",
    name = Name(
      forename = "Paul",
      middleName = "John",
      surname = "Smith",
    ),
    dateOfBirth = "2000-03-01",
    gender = null,
    sentence = CaseSentence(date = "2021-03-03", sentencingCourt = SentencingCourt("Fakecourt"), releaseDate = "2021-10-10"),
    responsibleProvider = ResponsibleProvider(
      code = "999000ABC",
      name = "Fakeprovider",
    ),
    ogrsScore = 123,
    age = 23,
    ageAtRelease = 24,
  )
}
