package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class OffenderAliasTest : DescribeSpec(
  {
    describe("#toAlias") {
      it("maps one-to-one attributes to alias attributes") {
        val offenderAlias =
          OffenderAlias(
            firstName = "Alias First Name",
            surname = "Alias Last Name",
            middleNames = listOf("Alias", "Middle", "Names"),
            dateOfBirth = LocalDate.parse("2023-01-01"),
            gender = "Gender",
          )

        val alias = offenderAlias.toAlias()

        alias.firstName.shouldBe(offenderAlias.firstName)
        alias.lastName.shouldBe(offenderAlias.surname)
        alias.middleName.shouldBe("Alias Middle Names")
        alias.dateOfBirth.shouldBe(offenderAlias.dateOfBirth)
        alias.gender.shouldBe(offenderAlias.gender)
      }

      it("returns null when no middle names") {
        val offenderAlias =
          OffenderAlias(
            firstName = "First Name",
            surname = "Surname",
            middleNames = listOf(),
          )

        val alias = offenderAlias.toAlias()

        alias.middleName.shouldBeNull()
      }
    }
  },
)
