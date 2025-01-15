package uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CourtAppearance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CourtDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Name
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ResponsibleProvider

fun generateCaseDetail(): CaseDetail =
  CaseDetail(
    nomsId = "ABC123",
    name =
      Name(
        forename = "Paul",
        middleName = "John",
        surname = "Smith",
      ),
    dateOfBirth = "2000-03-01",
    gender = null,
    courtAppearance =
      CourtAppearance(
        date = "2019-10-10",
        court =
          CourtDetails(
            name = "Crown",
          ),
      ),
    sentence = CaseSentence(expectedReleaseDate = "2021-10-10"),
    responsibleProvider =
      ResponsibleProvider(
        code = "999000ABC",
        name = "Fakeprovider",
      ),
    ogrsScore = 123,
    age = 23,
    ageAtRelease = 24,
  )
