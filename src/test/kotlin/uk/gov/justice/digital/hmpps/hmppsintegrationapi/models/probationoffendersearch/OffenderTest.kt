package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Alias
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
            OffenderAlias(firstName = "Alias First Name", surname = "Alias Surname"),
          ),
          otherIds = OtherIds(
            pncNumber = "pncNumber",
            nomsNumber = "nomsNumber",
            croNumber = "croNumber",
            crn = "crn",
          ),
        )

        val person = prisoner.toPerson()

        person.firstName.shouldBe(prisoner.firstName)
        person.lastName.shouldBe(prisoner.surname)
        person.middleName.shouldBe("Middle Name")
        person.dateOfBirth.shouldBe(prisoner.dateOfBirth)
        person.gender.shouldBe(prisoner.gender)
        person.ethnicity.shouldBe(prisoner.offenderProfile.ethnicity)
        person.aliases.first().shouldBeTypeOf<Alias>()
        person.identifiers.nomisNumber.shouldBe(prisoner.otherIds.nomsNumber)
        person.identifiers.croNumber.shouldBe(prisoner.otherIds.croNumber)
        person.identifiers.deliusCrn.shouldBe(prisoner.otherIds.crn)
        person.pncId.shouldBe(prisoner.otherIds.pncNumber)
      }

      it("can deal with missing fields") {
        val offender = Offender(
          firstName = "First Name",
          surname = "Surname",
        )

        val person = offender.toPerson()

        person.firstName.shouldBe(offender.firstName)
        person.lastName.shouldBe(offender.surname)
        person.middleName.shouldBe("")
        person.dateOfBirth.shouldBeNull()
        person.gender.shouldBeNull()
        person.ethnicity.shouldBeNull()
        person.aliases.shouldBeEmpty()
        person.identifiers.nomisNumber.shouldBeNull()
        person.identifiers.croNumber.shouldBeNull()
        person.identifiers.deliusCrn.shouldBeNull()
        person.pncId.shouldBeNull()
      }
    }
  },
)
