package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ndelius

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.text.format

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NDeliusGateway::class, FeatureFlagConfig::class],
)
class DeliusGatewaySearchTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val nDeliusGateway: NDeliusGateway,
) : DescribeSpec({
    val nDeliusMockServer = ApiMockServer.Companion.create(UpstreamApi.NDELIUS)
    val path = "/search/probation-cases"

    beforeEach {
      nDeliusMockServer.start()
      Mockito.reset(hmppsAuthGateway)

      whenever(hmppsAuthGateway.getClientToken("nDelius")).thenReturn(HmppsAuthMockServer.Companion.TOKEN)
    }

    afterTest {
      nDeliusMockServer.stop()
    }

    describe("#getPersons") {
      val firstName = "Matt"
      val surname = "Nolan"
      val pncNumber = "2018/0123456X"
      val dateOfBirth = "1966-10-25"
      val dateOfBirthString = dateOfBirth.format(DateTimeFormatter.ISO_DATE)

      beforeEach {
        nDeliusMockServer.stubForPost(
          path,
          """
            {
              "firstName": "$firstName",
              "surname": "$surname",
              "pncNumber": "$pncNumber",
              "dateOfBirth": "$dateOfBirthString",
              "includeAliases": false
            }
          """.removeWhitespaceAndNewlines(),
          File(
            "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/probationoffendersearch/fixtures/GetOffendersResponse.json",
          ).readText(),
        )
      }

      it("authenticates using HMPPS Auth with credentials") {
        nDeliusGateway.getPersons(firstName, surname, pncNumber, dateOfBirth)
        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("nDelius")
      }

      it("returns person(s) when searching on first name, last name, pnc number and date of birth") {
        val response = nDeliusGateway.getPersons(firstName, surname, pncNumber, dateOfBirth)

        response.data.count().shouldBe(1)
        response.data
          .first()
          .firstName
          .shouldBe(firstName)
        response.data
          .first()
          .lastName
          .shouldBe(surname)
        response.data
          .first()
          .pncId
          .shouldBe(pncNumber)
        response.data
          .first()
          .dateOfBirth
          .shouldBe(LocalDate.parse(dateOfBirth))
      }

      it("returns person(s) when searching on first name and last name") {
        nDeliusMockServer.stubForPost(
          path,
          """
        {
          "firstName": "Ahsoka",
          "surname": "Tano",
          "includeAliases": false
        }
        """.removeWhitespaceAndNewlines(),
          """
          [
            {
              "firstName": "Ahsoka",
              "surname": "Tano"
            }
          ]
          """.trimIndent(),
        )

        val response = nDeliusGateway.getPersons("Ahsoka", "Tano", null, null)

        response.data.count().shouldBe(1)
        response.data
          .first()
          .firstName
          .shouldBe("Ahsoka")
        response.data
          .first()
          .lastName
          .shouldBe("Tano")
      }

      it("returns person(s) when searching on first name and last name") {
        nDeliusMockServer.stubForPost(
          path,
          """
        {
          "firstName": "Ahsoka",
          "surname": "Tano",
          "includeAliases": false
        }
        """.removeWhitespaceAndNewlines(),
          """
          [
            {
              "firstName": "Ahsoka",
              "surname": "Tano"
            }
          ]
          """.trimIndent(),
        )

        val response = nDeliusGateway.getPersons("Ahsoka", "Tano", null, null)

        response.data.count().shouldBe(1)
        response.data
          .first()
          .firstName
          .shouldBe("Ahsoka")
        response.data
          .first()
          .lastName
          .shouldBe("Tano")
      }

      it("returns person(s) when searching on first name only") {
        nDeliusMockServer.stubForPost(
          path,
          """
        {
          "firstName": "Ahsoka",
          "includeAliases": false
        }
        """.removeWhitespaceAndNewlines(),
          """
          [
            {
              "firstName": "Ahsoka",
              "surname": "Tano"
            }
          ]
          """.trimIndent(),
        )

        val response = nDeliusGateway.getPersons("Ahsoka", null, null, null)

        response.data.count().shouldBe(1)
        response.data
          .first()
          .firstName
          .shouldBe("Ahsoka")
        response.data
          .first()
          .lastName
          .shouldBe("Tano")
      }

      it("returns person(s) when searching on last name only") {
        nDeliusMockServer.stubForPost(
          path,
          """
        {
          "surname": "Tano",
          "includeAliases": false
        }
        """.removeWhitespaceAndNewlines(),
          """
          [
            {
              "firstName": "Ahsoka",
              "surname": "Tano"
            }
          ]
          """.trimIndent(),
        )

        val response = nDeliusGateway.getPersons(null, "Tano", null, null)

        response.data.count().shouldBe(1)
        response.data
          .first()
          .firstName
          .shouldBe("Ahsoka")
        response.data
          .first()
          .lastName
          .shouldBe("Tano")
      }

      it("returns person(s) when searching on pnc number only") {
        nDeliusMockServer.stubForPost(
          path,
          """
        {
          "pncNumber": "2018/0123456X",
          "includeAliases": false
        }
        """.removeWhitespaceAndNewlines(),
          """
          [
            {
              "firstName": "Ahsoka",
              "surname": "Tano"
            }
          ]
          """.trimIndent(),
        )

        val response = nDeliusGateway.getPersons(null, null, pncNumber, null)

        response.data.count().shouldBe(1)
        response.data
          .first()
          .firstName
          .shouldBe("Ahsoka")
        response.data
          .first()
          .lastName
          .shouldBe("Tano")
      }

      it("returns person(s) when searching on date of birth only") {
        nDeliusMockServer.stubForPost(
          path,
          """
        {
          "dateOfBirth": "1966-10-25",
          "includeAliases": false
        }
        """.removeWhitespaceAndNewlines(),
          """
          [
            {
              "firstName": "Ahsoka",
              "surname": "Tano"
            }
          ]
          """.trimIndent(),
        )

        val response = nDeliusGateway.getPersons(null, null, null, dateOfBirth)

        response.data.count().shouldBe(1)
        response.data
          .first()
          .firstName
          .shouldBe("Ahsoka")
        response.data
          .first()
          .lastName
          .shouldBe("Tano")
      }

      it("returns person(s) when searching within aliases") {
        nDeliusMockServer.stubForPost(
          path,
          """
        {
          "firstName": "Fulcrum",
          "includeAliases": true
        }
        """.removeWhitespaceAndNewlines(),
          """
          [
            {
              "firstName": "Ahsoka",
              "surname": "Tano",
              "offenderAliases": [
                {
                  "firstName": "Fulcrum",
                  "surname": "Tano"
                }
              ]
            }
          ]
          """.trimIndent(),
        )

        val response = nDeliusGateway.getPersons("Fulcrum", null, null, null, searchWithinAliases = true)

        response.data.count().shouldBe(1)
        response.data
          .first()
          .aliases
          .first()
          .firstName
          .shouldBe("Fulcrum")
        response.data
          .first()
          .aliases
          .first()
          .lastName
          .shouldBe("Tano")
      }
    }

    describe("#getPerson") {

      describe("when a Delius CRN is used to make requests") {
        val hmppsId = "X777776"

        beforeEach {
          nDeliusMockServer.stubForPost(
            path,
            "{\"crn\": \"$hmppsId\"}",
            """
        [
          {
            "firstName": "Jonathan",
            "middleNames": [
              "Fred"
            ],
            "surname": "Bravo",
            "dateOfBirth": "1970-02-07",
            "offenderAliases": [],
          }
        ]
      """,
          )
        }

        it("calls the Probation API service with a Delius CRN") {
          nDeliusMockServer.stubForPost(
            path,
            "{\"crn\": \"$hmppsId\"}",
            """
          [
           {
            "firstName": "Jonathan",
            "surname": "Bravo",
            "dateOfBirth": "1970-02-07",
            "offenderAliases": []
          }
        ]
        """,
          )

          nDeliusGateway.getOffender(hmppsId)
        }
      }

      describe("when a Nomis number is used to make requests") {
        val hmppsId = "A7777ZZ"

        it("calls the Probation API service with a Nomis number") {
          nDeliusMockServer.stubForPost(
            path,
            "{\"nomsNumber\": \"$hmppsId\"}",
            """
          [
           {
            "firstName": "Jonathan",
            "surname": "Bravo",
            "dateOfBirth": "1970-02-07",
            "offenderAliases": []
          }
        ]
        """,
          )

          val response = nDeliusGateway.getOffender(hmppsId)

          response.data?.firstName.shouldBe("Jonathan")
          response.data?.surname.shouldBe("Bravo")
        }
      }
    }
  })
