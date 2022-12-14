package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NomisApiMockServer
import java.time.LocalDate

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NomisGateway::class, HmppsAuthGateway::class]
)
class NomisGatewayTest(@MockBean val hmppsAuthGateway: HmppsAuthGateway, private val nomisGateway: NomisGateway) :
  DescribeSpec({
    val nomisApiMockServer = NomisApiMockServer()
    val offenderNo = "abc123"

    beforeEach {
      nomisApiMockServer.start()
      nomisApiMockServer.stubGetOffender(
        offenderNo,
        """
        { 
          "offenderNo": "$offenderNo",
          "firstName": "John",
          "middleName": "Muriel",
          "lastName": "Smith",
          "dateOfBirth": "1970-03-15",
          "aliases": [
            {
              "firstName": "Joey",
              "middleName": "Martin",
              "lastName": "Smiles",
              "dob": "1975-10-12"
            }
          ]
        }
        """
      )

      Mockito.`when`(hmppsAuthGateway.getClientToken(any())).thenReturn(
        HmppsAuthMockServer.TOKEN
      )
    }

    afterTest {
      nomisApiMockServer.stop()
    }

    describe("#getPerson") {
      it("authenticates using HMPPS Auth with credentials") {
        nomisGateway.getPerson(offenderNo)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken(any())
      }

      it("returns a person with the matching ID") {
        val person = nomisGateway.getPerson(offenderNo)

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
        nomisApiMockServer.stubGetOffender(
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

        val person = nomisGateway.getPerson(offenderNo)

        person?.aliases.shouldBeEmpty()
      }
    }
  })
