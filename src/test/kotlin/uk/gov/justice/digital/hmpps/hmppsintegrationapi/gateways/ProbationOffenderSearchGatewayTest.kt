package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ProbationOffenderSearchApiMockServer

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonerOffenderSearchGateway::class, HmppsAuthGateway::class]
)
class ProbationOffenderSearchGatewayTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  private val probationOffenderSearchGateway: ProbationOffenderSearchGateway
) : DescribeSpec({
  val probationOffenderSearchApiMockServer = ProbationOffenderSearchApiMockServer()
  val nomsNumber = "xyz4321"

  beforeEach {
    probationOffenderSearchApiMockServer.start()
    probationOffenderSearchApiMockServer.stubGetPrisoner(
      nomsNumber,
      """
      """
    )

    val test = ProbationOffenderSearchGatewayTest::class // just for debugging, delete
    whenever(hmppsAuthGateway.getClientToken(ProbationOffenderSearchGatewayTest::class.simpleName!!)).thenReturn(HmppsAuthMockServer.TOKEN)
  }

  afterTest {
    probationOffenderSearchApiMockServer.stop()
  }

  describe("#getPerson") {
    it("authenticates using HMPPS Auth with credentials") {
      probationOffenderSearchGateway.getPerson(nomsNumber)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken(ProbationOffenderSearchGatewayTest::class.simpleName!!)
    }

    it("returns a person with the matching ID") {
      val person = probationOffenderSearchGateway.getPerson(nomsNumber)

      /*
      person?.firstName.shouldBe("John")
      person?.middleName.shouldBe("Muriel")
      person?.lastName.shouldBe("Smith")
      person?.dateOfBirth.shouldBe(LocalDate.parse("1970-03-15"))
      person?.aliases?.first()?.firstName.shouldBe("Joey")
      person?.aliases?.first()?.middleName.shouldBe("Martin")
      person?.aliases?.first()?.lastName.shouldBe("Smiles")
      person?.aliases?.first()?.dateOfBirth.shouldBe(LocalDate.parse("1975-10-12"))
       */
    }

    it("returns a person without aliases when no aliases are found") {
      probationOffenderSearchApiMockServer.stubGetPrisoner(
        nomsNumber,
        """
        """
      )

      val person = probationOffenderSearchGateway.getPerson(nomsNumber)

      person?.aliases.shouldBeEmpty()
    }
  }
})
