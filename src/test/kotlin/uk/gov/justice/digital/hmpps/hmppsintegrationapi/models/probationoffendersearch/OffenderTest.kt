package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import io.kotest.matchers.types.shouldBeTypeOf
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Alias
import java.time.LocalDate

class OffenderTest : DescribeSpec(
  {
    describe("#toPerson") {
      it("maps one-to-one attributes to person attributes") {
        val offender =
          Offender(
            firstName = "First Name",
            surname = "Surname",
            middleNames = listOf("Middle Name"),
            dateOfBirth = LocalDate.parse("2023-03-01"),
            gender = "Gender",
            offenderProfile = OffenderProfile(ethnicity = "Ethnicity"),
            offenderAliases =
              listOf(
                OffenderAlias(firstName = "Alias First Name", surname = "Alias Surname"),
              ),
            otherIds =
              OtherIds(
                pncNumber = "pncNumber",
                nomsNumber = "nomsNumber",
                croNumber = "croNumber",
                crn = "crn",
              ),
          )

        val person = offender.toPerson()

        person.firstName.shouldBe(offender.firstName)
        person.lastName.shouldBe(offender.surname)
        person.middleName.shouldBe("Middle Name")
        person.dateOfBirth.shouldBe(offender.dateOfBirth)
        person.gender.shouldBe(offender.gender)
        person.ethnicity.shouldBe(offender.offenderProfile.ethnicity)
        person.aliases.first().shouldBeTypeOf<Alias>()
        person.identifiers.nomisNumber.shouldBe(offender.otherIds.nomsNumber)
        person.identifiers.croNumber.shouldBe(offender.otherIds.croNumber)
        person.identifiers.deliusCrn.shouldBe(offender.otherIds.crn)
        person.pncId.shouldBe(offender.otherIds.pncNumber)
        person.hmppsId.shouldBe(offender.otherIds.crn)
      }

      it("returns null when no middle names") {
        val prisoner =
          Offender(
            firstName = "First Name",
            surname = "Surname",
            middleNames = listOf(),
          )

        val person = prisoner.toPerson()

        person.middleName.shouldBeEmpty()
      }
    }
  },
)
