package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Alias
import java.time.LocalDate

class PrisonerTest : DescribeSpec(
  {
    describe("#toPerson") {
      it("maps one-to-one attributes to person attributes") {

        val prisoner =
          POSPrisoner(
            firstName = "First Name",
            lastName = "Last Name",
            middleNames = "Middle Name",
            dateOfBirth = LocalDate.parse("2023-03-01"),
            gender = "Gender",
            ethnicity = "Ethnicity",
            prisonerNumber = "prisonerNumber",
            pncNumber = "pncNumber",
            croNumber = "croNumber",
            aliases =
              listOf(
                POSPrisonerAlias(firstName = "Alias First Name", lastName = "Alias Last Name"),
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
        person.identifiers.nomisNumber.shouldBe(prisoner.prisonerNumber)
        person.identifiers.croNumber.shouldBe(prisoner.croNumber)
        person.identifiers.deliusCrn.shouldBeNull()
        person.pncId.shouldBe(prisoner.pncNumber)
      }
    }
    describe("#toPESPrisonerDetails") {
      it("maps one-to-one attributes to PESPrisonerDetails attributes") {

        val prisoner =
          POSPrisoner(
            firstName = "First Name",
            lastName = "Last Name",
            middleNames = "Middle Name",
            dateOfBirth = LocalDate.parse("2023-03-01"),
            gender = "Gender",
            ethnicity = "Ethnicity",
            prisonerNumber = "prisonerNumber",
            pncNumber = "pncNumber",
            croNumber = "croNumber",
            aliases =
              listOf(
                POSPrisonerAlias(firstName = "Alias First Name", lastName = "Alias Last Name"),
              ),
            prisonId = "LEI",
            prisonName = "HMP Leeds",
            cellLocation = "6-2-006",
          )

        val pesPrisoner = prisoner.toPESPrisonerDetails()
        pesPrisoner.prisonerNumber.shouldBe(prisoner.prisonerNumber)
        pesPrisoner.firstName.shouldBe(prisoner.firstName)
        pesPrisoner.lastName.shouldBe(prisoner.lastName)
        pesPrisoner.prisonId.shouldBe(prisoner.prisonId)
        pesPrisoner.prisonName.shouldBe(prisoner.prisonName)
        pesPrisoner.cellLocation.shouldBe(prisoner.cellLocation)
      }
    }
  },
)
