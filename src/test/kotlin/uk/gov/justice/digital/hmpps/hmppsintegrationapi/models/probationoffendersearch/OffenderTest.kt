package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class OffenderTest : DescribeSpec(
  {
    describe("#toPerson") {
      it("maps one-to-one attributes to person attributes") {
        val prisoner = Offender(
          firstName = "First Name",
          surname = "Surname",
          middleNames = listOf("Middle Name"),
          dateOfBirth = LocalDate.parse("2023-03-01"),
          gender = "Gender",
          offenderProfile = OffenderProfile(ethnicity = "Ethnicity"),
          offenderAliases = listOf(
            OffenderAlias(
              firstName = "Alias First Name",
              surname = "Alias Surname",
              middleNames = listOf("Alias Middle Name"),
              dateOfBirth = LocalDate.parse("2023-03-01"),
            ),
          ),
          otherIds = OtherIds(pncNumber = "pncNumber"),
        )

        val person = prisoner.toPerson()

        person.firstName.shouldBe(prisoner.firstName)
        person.lastName.shouldBe(prisoner.surname)
        person.middleName.shouldBe("Middle Name")
        person.dateOfBirth.shouldBe(prisoner.dateOfBirth)
        person.gender.shouldBe(prisoner.gender)
        person.ethnicity.shouldBe(prisoner.offenderProfile.ethnicity)
        person.aliases.first().firstName.shouldBe("Alias First Name")
        person.aliases.first().lastName.shouldBe("Alias Surname")
        person.aliases.first().middleName.shouldBe("Alias Middle Name")
        person.aliases.first().dateOfBirth.shouldBe(LocalDate.parse("2023-03-01"))
        person.pncId.shouldBe(prisoner.otherIds.pncNumber)
      }
    }
  },
)
