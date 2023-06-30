package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Alias
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
            PrisonerAlias(firstName = "Alias First Name", lastName = "Alias Last Name"),
          ),
        )

        val person = prisoner.toPerson()

        person.firstName.shouldBe(prisoner.firstName)
        person.lastName.shouldBe(prisoner.lastName)
        person.middleName.shouldBe(prisoner.middleNames)
        person.dateOfBirth.shouldBe(prisoner.dateOfBirth)
        person.gender.shouldBe(prisoner.gender)
        person.ethnicity.shouldBe(prisoner.ethnicity)
        person.aliases.first().shouldBeTypeOf<Alias>()
        person.nomisNumber.shouldBe(prisoner.prisonerNumber)
        person.pncId.shouldBe(prisoner.pncNumber)
      }
    }
  },
)
