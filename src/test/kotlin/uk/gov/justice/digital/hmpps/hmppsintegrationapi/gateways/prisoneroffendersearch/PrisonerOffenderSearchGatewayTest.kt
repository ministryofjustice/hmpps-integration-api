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
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.PrisonerOffenderSearchApiMockServer
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonerOffenderSearchGateway::class],
)
class PrisonerOffenderSearchGatewayTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  private val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
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

  describe("#getPersons") {
    val firstName = "JAMES"
    val lastName = "HOWLETT"
    val hmppsId = "2003/13116A"

    beforeEach {
      prisonerOffenderSearchApiMockServer.stubPostPrisonerSearch(
        """
            {
              "firstName": "$firstName",
              "lastName": "$lastName",
              "prisonerIdentifier": "$hmppsId",
              "includeAliases": false
            }
          """.removeWhitespaceAndNewlines(),
        File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/prisoneroffendersearch/fixtures/GetPrisonersResponse.json").readText(),
      )
    }

    it("authenticates using HMPPS Auth with credentials") {
      prisonerOffenderSearchGateway.getPersons(firstName, lastName, hmppsId)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("Prisoner Offender Search")
    }

    it("returns person(s) when searching on Hmpps ID, first and last name") {
      val response = prisonerOffenderSearchGateway.getPersons(firstName, lastName, hmppsId)

      response.data.count().shouldBe(4)
      response.data.forEach {
        it.firstName.shouldBe(firstName)
        it.lastName.shouldBe(lastName)
      }
      response.data[0].identifiers.nomisNumber.shouldBe("A7796DY")
      response.data[1].identifiers.nomisNumber.shouldBe("G9347GV")
      response.data[2].identifiers.nomisNumber.shouldBe("A5043DY")
      response.data[3].identifiers.nomisNumber.shouldBe("A5083DY")

      response.data[0].pncId.shouldBeNull()
      response.data[1].pncId.shouldBe("95/289622B")
      response.data[2].pncId.shouldBeNull()
      response.data[3].pncId.shouldBe("03/11985X")
    }

    it("returns person(s) when searching on first name only") {
      prisonerOffenderSearchApiMockServer.stubPostPrisonerSearch(
        """
        {
          "firstName": "Obi-Wan",
          "includeAliases": false
        }
        """.removeWhitespaceAndNewlines(),
        """
        {
          "content": [
            {
              "firstName": "Obi-Wan",
              "lastName": "Kenobi"
            }
          ]
        }
        """.trimIndent(),
      )

      val response = prisonerOffenderSearchGateway.getPersons("Obi-Wan", null)

      response.data.count().shouldBe(1)
      response.data.first().firstName.shouldBe("Obi-Wan")
      response.data.first().lastName.shouldBe("Kenobi")
    }

    it("returns person(s) when searching on hmppsId only") {
      prisonerOffenderSearchApiMockServer.stubPostPrisonerSearch(
        """
        {
          "prisonerIdentifier": "$hmppsId",
          "includeAliases": false
        }
        """.removeWhitespaceAndNewlines(),
        """
        {
          "content": [
            {
              "firstName": "Obi-Wan",
              "lastName": "Kenobi",
              "prisonerNumber": "A1234AA"
            }
          ]
        }
        """.trimIndent(),
      )

      val response = prisonerOffenderSearchGateway.getPersons(hmppsId = hmppsId)

      response.data.count().shouldBe(1)
      response.data.first().firstName.shouldBe("Obi-Wan")
      response.data.first().lastName.shouldBe("Kenobi")
      response.data.first().identifiers.nomisNumber.shouldBe("A1234AA")
    }

    it("returns person(s) when searching on last name only") {
      prisonerOffenderSearchApiMockServer.stubPostPrisonerSearch(
        """
        {
          "lastName": "Binks",
          "includeAliases": false
        }
        """.removeWhitespaceAndNewlines(),
        """
        {
          "content": [
            {
              "firstName": "Jar Jar",
              "lastName": "Binks"
            }
          ]
        }
        """.trimIndent(),
      )

      val response = prisonerOffenderSearchGateway.getPersons(null, "Binks")

      response.data.count().shouldBe(1)
      response.data.first().firstName.shouldBe("Jar Jar")
      response.data.first().lastName.shouldBe("Binks")
    }

    it("returns person(s) when searching within aliases") {
      prisonerOffenderSearchApiMockServer.stubPostPrisonerSearch(
        """
        {
          "firstName": "Geralt",
          "includeAliases": true
        }
        """.removeWhitespaceAndNewlines(),
        """
        {
          "content": [
            {
              "firstName": "Rich",
              "lastName": "Roger",
              "aliases": [
                {
                  "firstName": "Geralt",
                  "lastName": "Eric du Haute-Bellegarde"
                }
              ]
            }
          ]
        }
        """.trimIndent(),
      )

      val response = prisonerOffenderSearchGateway.getPersons("Geralt", null, searchWithinAliases = true)

      response.data.count().shouldBe(1)
      response.data.first().aliases.first().firstName.shouldBe("Geralt")
      response.data.first().aliases.first().lastName.shouldBe("Eric du Haute-Bellegarde")
    }

    it("returns an empty list of Person if no matching person") {
      val firstNameThatDoesNotExist = "ZYX321"
      val lastNameThatDoesNotExist = "GHJ345"

      prisonerOffenderSearchApiMockServer.stubPostPrisonerSearch(
        """
            {
              "firstName": "$firstNameThatDoesNotExist",
              "lastName": "$lastNameThatDoesNotExist",
              "includeAliases": false
            }
          """.removeWhitespaceAndNewlines(),
        """
        {
          "content": []
        }
        """,
      )

      val response = prisonerOffenderSearchGateway.getPersons(firstNameThatDoesNotExist, lastNameThatDoesNotExist)

      response.data.shouldBeEmpty()
    }
  }
},)
