package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class PrisonerTest : DescribeSpec(
  {
    describe("#toPerson") {
      it("maps one-to-one attributes to person attributes") {
        val prisoner = Prisoner(
          firstName = "First Name",
          lastName = "Last Name",
          middleNames = "Middle Name",
          dateOfBirth = LocalDate.parse("2023-03-01"),
          gender = "Gender",
          ethnicity = "Ethnicity",
          prisonerNumber = "prisonerNumber",
          pncNumber = "pncNumber",
          aliases = listOf(
            PrisonerAlias(
              firstName = "Alias First Name",
              lastName = "Alias Last Name",
              middleNames = "Alias Middle Names",
              dateOfBirth = LocalDate.parse("2023-01-01"),
            ),
          ),
        )

        val person = prisoner.toPerson()

        person.firstName.shouldBe(prisoner.firstName)
        person.lastName.shouldBe(prisoner.lastName)
        person.middleName.shouldBe(prisoner.middleNames)
        person.dateOfBirth.shouldBe(prisoner.dateOfBirth)
        person.gender.shouldBe(prisoner.gender)
        person.ethnicity.shouldBe(prisoner.ethnicity)
        person.aliases.first().firstName.shouldBe("Alias First Name")
        person.aliases.first().lastName.shouldBe("Alias Last Name")
        person.aliases.first().middleName.shouldBe("Alias Middle Names")
        person.aliases.first().dateOfBirth.shouldBe(LocalDate.parse("2023-01-01"))
        person.prisonerId.shouldBe(prisoner.prisonerNumber)
        person.pncId.shouldBe(prisoner.pncNumber)
      }
    }
  },
)
