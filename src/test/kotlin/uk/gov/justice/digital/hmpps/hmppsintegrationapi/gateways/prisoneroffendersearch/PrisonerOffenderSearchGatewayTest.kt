package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.prisoneroffendersearch

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.PrisonerOffenderSearchApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File
import java.time.LocalDate

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonerOffenderSearchGateway::class],
)
class PrisonerOffenderSearchGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
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
      val dateOfBirth = "1975-02-28"

      beforeEach {
        prisonerOffenderSearchApiMockServer.stubPostPrisonerSearch(
          """
            {
              "firstName": "$firstName",
              "lastName": "$lastName",
              "dateOfBirth": "$dateOfBirth",
              "includeAliases": false
            }
          """.removeWhitespaceAndNewlines(),
          File(
            "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/prisoneroffendersearch/fixtures/GetPrisonersResponse.json",
          ).readText(),
        )
      }

      it("authenticates using HMPPS Auth with credentials") {
        prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("Prisoner Offender Search")
      }

      it("returns person(s) when searching on first name, last name and date of birth, in a descending order according to date of birth") {
        val response = prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth)

        response.data.count().shouldBe(4)
        response.data.forEach {
          it.firstName.shouldBe(firstName)
          it.lastName.shouldBe(lastName)
        }
        response.data[0].identifiers.nomisNumber.shouldBe("A5043DY")
        response.data[1].identifiers.nomisNumber.shouldBe("A5083DY")
        response.data[2].identifiers.nomisNumber.shouldBe("G9347GV")
        response.data[3].identifiers.nomisNumber.shouldBe("A7796DY")

        response.data[0].pncId.shouldBeNull()
        response.data[1].pncId.shouldBe("03/11985X")
        response.data[2].pncId.shouldBe("95/289622B")
        response.data[3].pncId.shouldBeNull()
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

        val response = prisonerOffenderSearchGateway.getPersons("Obi-Wan", null, null)

        response.data.count().shouldBe(1)
        response.data.first().firstName.shouldBe("Obi-Wan")
        response.data.first().lastName.shouldBe("Kenobi")
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

        val response = prisonerOffenderSearchGateway.getPersons(null, "Binks", null)

        response.data.count().shouldBe(1)
        response.data.first().firstName.shouldBe("Jar Jar")
        response.data.first().lastName.shouldBe("Binks")
      }

      it("returns person(s) when searching on date of birth only") {
        prisonerOffenderSearchApiMockServer.stubPostPrisonerSearch(
          """
        {
          "includeAliases": false,
          "dateOfBirth": "1975-02-28"
        }
        """.removeWhitespaceAndNewlines(),
          """
          {
            "content": [
              {
                "firstName": "Jar Jar",
                "lastName": "Binks",
                "dateOfBirth": "1975-02-28"
              }
            ]
          }
          """.trimIndent(),
        )

        val response = prisonerOffenderSearchGateway.getPersons(null, null, dateOfBirth)

        response.data.count().shouldBe(1)
        response.data.first().firstName.shouldBe("Jar Jar")
        response.data.first().lastName.shouldBe("Binks")
        response.data.first().dateOfBirth.shouldBe(LocalDate.parse(dateOfBirth))
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

        val response = prisonerOffenderSearchGateway.getPersons("Geralt", null, null, true)

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

        val response = prisonerOffenderSearchGateway.getPersons(firstNameThatDoesNotExist, lastNameThatDoesNotExist, null)

        response.data.shouldBeEmpty()
      }
    }

    describe("#getPrisonOffender") {
      val nomsNumber = "mockNomsNumber"

      beforeEach {
        prisonerOffenderSearchApiMockServer.stubGetPrisoner(
          nomsNumber,
          """
           {
            "prisonerNumber": "A7796DY",
            "bookingId": "599877",
            "firstName": "JAMES",
            "middleNames": "MARTIN",
            "lastName": "HOWLETT",
            "maritalStatus": "Widowed"
          }
          """.trimIndent(),
        )
      }

      it("authenticates using HMPPS Auth with credentials") {
        prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("Prisoner Offender Search")
      }

      it("returns reasonable adjustment for a person with the matching ID") {
        val response = prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)

        response.data?.prisonerNumber.shouldBe("A7796DY")
        response.data?.bookingId.shouldBe("599877")
        response.data?.firstName.shouldBe("JAMES")
        response.data?.middleNames.shouldBe("MARTIN")
        response.data?.lastName.shouldBe("HOWLETT")
        response.data?.maritalStatus.shouldBe("Widowed")
      }

      it("returns an error when 404 NOT FOUND is returned") {
        prisonerOffenderSearchApiMockServer.stubGetPrisoner(
          nomsNumber,
          """
        {
          "developerMessage": "cannot find person"
        }
        """,
          HttpStatus.NOT_FOUND,
        )

        val response = prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)

        response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
      }
    }
  })
