package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.Need as ArnNeed
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.Needs as ArnNeeds
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Need as HmppsNeed
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Needs as HmppsNeeds

class NeedsTest : DescribeSpec(
  {
    describe("#toNeeds") {
      it("maps one-to-one attributes to Integration API attributes") {
        val arnNeeds = ArnNeeds(
          assessedOn = LocalDateTime.parse("2000-11-27T10:15:41"),
          identifiedNeeds = listOf(ArnNeed(section = "EDUCATION_TRAINING_AND_EMPLOYABILITY"), ArnNeed(section = "FINANCIAL_MANAGEMENT_AND_INCOME")),
          notIdentifiedNeeds = listOf(ArnNeed(section = "RELATIONSHIPS")),
          unansweredNeeds = listOf(ArnNeed(section = "LIFESTYLE_AND_ASSOCIATES"), ArnNeed(section = "DRUG_MISUSE"), ArnNeed(section = "ALCOHOL_MISUSE")),
        )

        val integrationApiNeeds = arnNeeds.toNeeds()

        integrationApiNeeds.assessedOn.shouldBe(arnNeeds.assessedOn)
        integrationApiNeeds.identifiedNeeds.shouldBe(
          listOf(
            HmppsNeed(type = "EDUCATION_TRAINING_AND_EMPLOYABILITY"),
            HmppsNeed(type = "FINANCIAL_MANAGEMENT_AND_INCOME"),
          ),
        )
        integrationApiNeeds.notIdentifiedNeeds.shouldBe(
          listOf(
            HmppsNeed(type = "RELATIONSHIPS"),
          ),
        )
        integrationApiNeeds.unansweredNeeds.shouldBe(
          listOf(
            HmppsNeed(type = "LIFESTYLE_AND_ASSOCIATES"),
            HmppsNeed(type = "DRUG_MISUSE"),
            HmppsNeed(type = "ALCOHOL_MISUSE"),
          ),
        )
      }

      it("can be constructed with NULL values") {
        val arnNeeds = ArnNeeds()

        val integrationApiNeeds = arnNeeds.toNeeds()

        integrationApiNeeds.shouldBe(
          HmppsNeeds(
            assessedOn = null,
            identifiedNeeds = emptyList(),
            notIdentifiedNeeds = emptyList(),
            unansweredNeeds = emptyList(),
          ),
        )
      }
    }
  },
)
