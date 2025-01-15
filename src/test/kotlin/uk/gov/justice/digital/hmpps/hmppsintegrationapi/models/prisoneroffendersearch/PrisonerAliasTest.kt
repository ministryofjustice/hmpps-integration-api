package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class PrisonerAliasTest :
  DescribeSpec(
    {
      describe("#toAlias") {
        it("maps one-to-one attributes to alias attributes") {
          val prisonerAlias =
            POSPrisonerAlias(
              firstName = "Alias First Name",
              lastName = "Alias Last Name",
              middleNames = "Alias Middle Names",
              dateOfBirth = LocalDate.parse("2023-01-01"),
              gender = "Gender",
              ethnicity = "Ethnicity",
            )

          val alias = prisonerAlias.toAlias()

          alias.firstName.shouldBe(prisonerAlias.firstName)
          alias.lastName.shouldBe(prisonerAlias.lastName)
          alias.middleName.shouldBe(prisonerAlias.middleNames)
          alias.dateOfBirth.shouldBe(prisonerAlias.dateOfBirth)
          alias.gender.shouldBe(prisonerAlias.gender)
          alias.ethnicity.shouldBe(prisonerAlias.ethnicity)
        }
      }
    },
  )
