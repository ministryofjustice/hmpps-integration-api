package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.PrisonerOffenderSearchApiMockServer
import java.time.LocalDate

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonerOffenderSearchGateway::class, HmppsAuthGateway::class]
)
class PrisonerOffenderSearchGatewayTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  private val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway
) : DescribeSpec({
  val prisonerOffenderSearchApiMockServer = PrisonerOffenderSearchApiMockServer()
  val offenderNo = "abc123"

  beforeEach {
    prisonerOffenderSearchApiMockServer.start()
    prisonerOffenderSearchApiMockServer.stubGetPrisoner(
      offenderNo,
      """
        {
          "offenderNo": "$offenderNo",
          "firstName": "John",
          "middleNames": "Muriel",
          "lastName": "Smith",
          "dateOfBirth": "1970-03-15",
          "aliases": [
            {
              "firstName": "Joey",
              "middleNames": "Martin",
              "lastName": "Smiles",
              "dob": "1975-10-12"
            }
          ]
        }
        """
    )

    Mockito.`when`(hmppsAuthGateway.getClientToken()).thenReturn(
      HmppsAuthMockServer.TOKEN
    )
  }

  afterTest {
    prisonerOffenderSearchApiMockServer.stop()
  }

  describe("#getPerson") {
    it("authenticates using HMPPS Auth with credentials") {
      prisonerOffenderSearchGateway.getPerson(offenderNo)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken()
    }

    it("returns a person with the matching ID") {
      val person = prisonerOffenderSearchGateway.getPerson(offenderNo)

      person?.firstName.shouldBe("John")
      person?.middleName.shouldBe("Muriel")
      person?.lastName.shouldBe("Smith")
      person?.dateOfBirth.shouldBe(LocalDate.parse("1970-03-15"))
      person?.aliases?.first()?.firstName.shouldBe("Joey")
      person?.aliases?.first()?.middleName.shouldBe("Martin")
      person?.aliases?.first()?.lastName.shouldBe("Smiles")
      person?.aliases?.first()?.dateOfBirth.shouldBe(LocalDate.parse("1975-10-12"))
    }

    it("returns a person without aliases when no aliases are found") {
      prisonerOffenderSearchApiMockServer.stubGetPrisoner(
        offenderNo,
        """
          {
            "offenderNo": "$offenderNo",
            "firstName": "John",
            "lastName": "Smith",
            "aliases": []
          }
          """
      )

      val person = prisonerOffenderSearchGateway.getPerson(offenderNo)

      person?.aliases.shouldBeEmpty()
    }
  }
})
