package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.prisoneroffendersearch

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.PrisonerOffenderSearchApiMockServer
import java.io.File
import java.time.LocalDate

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonerOffenderSearchGateway::class]
)
class PrisonerOffenderSearchGatewayTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  private val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway
) : DescribeSpec({
  val prisonerOffenderSearchApiMockServer = PrisonerOffenderSearchApiMockServer()

  beforeEach {
    prisonerOffenderSearchApiMockServer.start()
    Mockito.reset(hmppsAuthGateway)

    whenever(hmppsAuthGateway.getClientToken("Prisoner Offender Search")).thenReturn(HmppsAuthMockServer.TOKEN)
  }

  afterTest {
    prisonerOffenderSearchApiMockServer.stop()
  }

  describe("#getPerson(s)") {
    val firstName = "PETER"
    val lastName = "PHILLIPS"

    beforeEach {
      prisonerOffenderSearchApiMockServer.stubPostPrisonerSearch(
        """
            {
              "firstName":"$firstName",
              "lastName":"$lastName",
              "includeAliases":true
            }
          """.removeWhitespaceAndNewlines(),
        File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/prisoneroffendersearch/fixtures/GetPrisonersResponse.json").readText()
      )
    }

    it("authenticates using HMPPS Auth with credentials") {
      prisonerOffenderSearchGateway.getPersons(firstName, lastName)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("Prisoner Offender Search")
    }

    it("returns person(s) when performing a search") {
      val persons = prisonerOffenderSearchGateway.getPersons(firstName, lastName)

      persons.count().shouldBe(4)
      persons.forEach {
        it?.firstName.shouldBe(firstName)
        it?.lastName.shouldBe(lastName)
      }
    }

    it("returns an empty list of Person if no matching person") {
      val firstNameThatDoesNotExist = "ZYX321"
      val lastNameThatDoesNotExist = "GHJ345"

      prisonerOffenderSearchApiMockServer.stubPostPrisonerSearch(
        """
            {
              "firstName":"$firstNameThatDoesNotExist",
              "lastName":"$lastNameThatDoesNotExist",
              "includeAliases":true
            }
          """.removeWhitespaceAndNewlines(),
        """
        {
          "content": []
        }
        """
      )

      val persons = prisonerOffenderSearchGateway.getPersons(firstNameThatDoesNotExist, lastNameThatDoesNotExist)

      persons.shouldBeEmpty()
    }
  }

  describe("#getPerson") {
    val offenderNo = "abc123"

    beforeEach {
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
              "dateOfBirth": "1975-10-12"
            }
          ]
        }
        """
      )
    }

    it("authenticates using HMPPS Auth with credentials") {
      prisonerOffenderSearchGateway.getPerson(offenderNo)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("Prisoner Offender Search")
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

    it("returns null when 400 Bad Request is returned") {
      prisonerOffenderSearchApiMockServer.stubGetPrisoner(
        offenderNo,
        """
          {
            "developerMessage": "reason for bad request"
          }
          """,
        HttpStatus.BAD_REQUEST
      )

      val person = prisonerOffenderSearchGateway.getPerson(offenderNo)

      person?.shouldBeNull()
    }

    it("returns null when 404 Not Found is returned") {
      prisonerOffenderSearchApiMockServer.stubGetPrisoner(
        offenderNo,
        """
          {
            "developerMessage": "cannot find person"
          }
          """,
        HttpStatus.NOT_FOUND
      )

      val person = prisonerOffenderSearchGateway.getPerson(offenderNo)

      person?.shouldBeNull()
    }
  }
})
