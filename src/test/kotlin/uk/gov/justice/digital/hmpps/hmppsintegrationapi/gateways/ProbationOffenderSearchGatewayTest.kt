package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ProbationOffenderSearchApiMockServer
import java.time.LocalDate

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ProbationOffenderSearchGateway::class, HmppsAuthGateway::class],
)
class ProbationOffenderSearchGatewayTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  private val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
) : DescribeSpec({
  val probationOffenderSearchApiMockServer = ProbationOffenderSearchApiMockServer()
  val nomsNumber = "xyz4321"

  beforeEach {
    probationOffenderSearchApiMockServer.start()
    probationOffenderSearchApiMockServer.stubPostOffenderSearch(
      "{\"nomsNumber\": \"$nomsNumber\"}",
      """
        [
           {
            "firstName": "Jonathan",
            "surname": "Bravo",
            "dateOfBirth": "1970-02-07",
            "otherIds": {
              "nomsNumber": "$nomsNumber"
            },
            "offenderAliases": [
              {
                "dateOfBirth": "2000-02-07",
                "firstName": "John",
                "surname": "Wick"
              }
            ]
          }
        ]
      """,
    )

    whenever(hmppsAuthGateway.getClientToken("Probation Offender Search")).thenReturn(
      HmppsAuthMockServer.TOKEN,
    )
  }

  afterTest {
    probationOffenderSearchApiMockServer.stop()
  }

  describe("#getPerson") {
    it("authenticates using HMPPS Auth with credentials") {
      probationOffenderSearchGateway.getPerson(nomsNumber)

      verify(
        hmppsAuthGateway,
        VerificationModeFactory.times(1),
      ).getClientToken("Probation Offender Search")
    }

    it("returns a person with the matching ID") {
      val person = probationOffenderSearchGateway.getPerson(nomsNumber)

      person?.firstName.shouldBe("Jonathan")
      person?.lastName.shouldBe("Bravo")
      person?.dateOfBirth.shouldBe(LocalDate.parse("1970-02-07"))
      person?.aliases?.first()?.firstName.shouldBe("John")
      person?.aliases?.first()?.lastName.shouldBe("Wick")
      person?.aliases?.first()?.dateOfBirth.shouldBe(LocalDate.parse("2000-02-07"))
    }

    it("returns a person without aliases when no aliases are found") {
      probationOffenderSearchApiMockServer.stubPostOffenderSearch(
        "{\"nomsNumber\": \"$nomsNumber\"}",
        """
          [
           {
            "firstName": "Jonathan",
            "surname": "Bravo",
            "dateOfBirth": "1970-02-07",
            "otherIds": {
              "nomsNumber": "$nomsNumber"
            },
            "offenderAliases": []
          }
        ]
        """,
      )

      val person = probationOffenderSearchGateway.getPerson(nomsNumber)

      person?.aliases.shouldBeEmpty()
    }
  }
})
