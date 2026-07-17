package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Need
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Needs
import java.time.LocalDateTime

class NeedsTest :
  DescribeSpec(
    {
      describe("#toNeeds") {
        it("maps one-to-one attributes to Integration API attributes") {
          val arnNeeds =
            ArnNeeds(
              assessedOn = LocalDateTime.parse("2000-11-27T10:15:41"),
              needs =
                listOf(
                  ArnNeed(section = "EDUCATION_TRAINING_AND_EMPLOYABILITY"),
                  ArnNeed(section = "FINANCIAL_MANAGEMENT_AND_INCOME"),
                  ArnNeed(section = "LIFESTYLE_AND_ASSOCIATES"),
                  ArnNeed(section = "DRUG_MISUSE"),
                  ArnNeed(section = "ALCOHOL_MISUSE"),
                ),
            )

          val integrationApiNeeds = arnNeeds.toNeeds()

          integrationApiNeeds.assessedOn.shouldBe(arnNeeds.assessedOn)
          integrationApiNeeds.needs.shouldBe(
            listOf(
              Need(type = "EDUCATION_TRAINING_AND_EMPLOYABILITY"),
              Need(type = "FINANCIAL_MANAGEMENT_AND_INCOME"),
              Need(type = "RELATIONSHIPS"),
              Need(type = "LIFESTYLE_AND_ASSOCIATES"),
              Need(type = "DRUG_MISUSE"),
              Need(type = "ALCOHOL_MISUSE"),
            ),
          )
        }

        it("can be constructed with NULL values") {
          val arnNeeds = ArnNeeds()

          val integrationApiNeeds = arnNeeds.toNeeds()

          integrationApiNeeds.shouldBe(
            Needs(
              assessedOn = null,
              needs = emptyList(),
            ),
          )
        }
      }
    },
  )
