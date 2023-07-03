package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Alias as HmppsAlias

class OffenderTest : DescribeSpec(
  {
    describe("#toPerson") {
      it("maps one-to-one attributes to integration API attributes") {
        val anAlias = Alias(
          firstName = "Bill",
          lastName = "Smith",
          middleName = "Jo",
          dob = LocalDate.now(),
        )

        val offender = Offender(
          firstName = "Billy",
          lastName = "Smith",
          middleName = "John",
          dateOfBirth = LocalDate.now(),
          aliases = listOf(anAlias),
        )

        val person = offender.toPerson()

        person.firstName.shouldBe(offender.firstName)
        person.lastName.shouldBe(offender.lastName)
        person.middleName.shouldBe(offender.middleName)
        person.dateOfBirth.shouldBe(offender.dateOfBirth)
        person.aliases.shouldBe(
          listOf(
            HmppsAlias(
              firstName = anAlias.firstName,
              lastName = anAlias.lastName,
              middleName = anAlias.middleName,
              dateOfBirth = anAlias.dob,
              gender = null,
              ethnicity = null,
            ),
          ),
        )
      }

      it("can deal with missing fields") {
        val offender = Offender(
          firstName = "Bob",
          lastName = "Smith",
        )

        val person = offender.toPerson()

        person.firstName.shouldBe(offender.firstName)
        person.lastName.shouldBe(offender.lastName)
        person.middleName.shouldBeNull()
        person.dateOfBirth.shouldBeNull()
        person.aliases.shouldBeEmpty()
      }
    }
  },
)
