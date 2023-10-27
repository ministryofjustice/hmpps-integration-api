package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.probationoffendersearch

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
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ProbationOffenderSearchApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import java.io.File
import java.time.LocalDate

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ProbationOffenderSearchGateway::class],
)
class ProbationOffenderSearchGatewayTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  private val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
) : DescribeSpec({
  val probationOffenderSearchApiMockServer = ProbationOffenderSearchApiMockServer()

  beforeEach {
    probationOffenderSearchApiMockServer.start()
    Mockito.reset(hmppsAuthGateway)

    whenever(hmppsAuthGateway.getClientToken("Probation Offender Search")).thenReturn(HmppsAuthMockServer.TOKEN)
  }

  afterTest {
    probationOffenderSearchApiMockServer.stop()
  }

  describe("#getPersons") {
    val firstName = "Matt"
    val surname = "Nolan"

    beforeEach {
      probationOffenderSearchApiMockServer.stubPostOffenderSearch(
        """
            {
              "firstName": "$firstName",
              "surname": "$surname",
              "includeAliases": false
            }
          """.removeWhitespaceAndNewlines(),
        File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/probationoffendersearch/fixtures/GetOffendersResponse.json").readText(),
      )
    }

    it("authenticates using HMPPS Auth with credentials") {
      probationOffenderSearchGateway.getPersons(firstName, surname)
      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("Probation Offender Search")
    }

    it("returns person(s) when searching on first and last name") {
      val response = probationOffenderSearchGateway.getPersons(firstName, surname)

      response.data.count().shouldBe(1)
      response.data.first().firstName.shouldBe(firstName)
      response.data.first().lastName.shouldBe(surname)
      response.data.first().pncId.shouldBe("2018/0123456X")
    }

    it("returns person(s) when searching on first name only") {
      probationOffenderSearchApiMockServer.stubPostOffenderSearch(
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

      val response = probationOffenderSearchGateway.getPersons("Ahsoka", null)

      response.data.count().shouldBe(1)
      response.data.first().firstName.shouldBe("Ahsoka")
      response.data.first().lastName.shouldBe("Tano")
    }

    it("returns person(s) when searching on last name only") {
      probationOffenderSearchApiMockServer.stubPostOffenderSearch(
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
      val response = probationOffenderSearchGateway.getPersons(null, "Tano")

      response.data.count().shouldBe(1)
      response.data.first().firstName.shouldBe("Ahsoka")
      response.data.first().lastName.shouldBe("Tano")
    }

    it("returns person(s) when searching within aliases") {
      probationOffenderSearchApiMockServer.stubPostOffenderSearch(
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

      val response = probationOffenderSearchGateway.getPersons("Fulcrum", null, searchWithinAliases = true)

      response.data.count().shouldBe(1)
      response.data.first().aliases.first().firstName.shouldBe("Fulcrum")
      response.data.first().aliases.first().lastName.shouldBe("Tano")
    }
  }

  describe("#getPerson") {
    describe("when useCrnInsteadOfPncId feature flag is false") {
      val hmppsId = "2002/1121M"
      beforeEach {
        probationOffenderSearchApiMockServer.stubPostOffenderSearch(
          "{\"pncNumber\": \"$hmppsId\"}",
          """
        [
           {
            "firstName": "Jonathan",
            "middleNames": [
              "Echo",
              "Fred"
            ],
            "surname": "Bravo",
            "dateOfBirth": "1970-02-07",
            "offenderAliases": [
              {
                "dateOfBirth": "2000-02-07",
                "firstName": "John",
                "middleNames": [
                  "Tom"
                ],
                "surname": "Wick"
              }
            ]
          }
        ]
      """,
        )
      }

      it("authenticates using HMPPS Auth with credentials") {
        probationOffenderSearchGateway.getPerson(hmppsId)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("Probation Offender Search")
      }

      it("returns a person with the matching ID") {
        val response = probationOffenderSearchGateway.getPerson(hmppsId)

        response.data?.firstName.shouldBe("Jonathan")
        response.data?.middleName.shouldBe("Echo Fred")
        response.data?.lastName.shouldBe("Bravo")
        response.data?.dateOfBirth.shouldBe(LocalDate.parse("1970-02-07"))
        response.data?.aliases?.first()?.firstName.shouldBe("John")
        response.data?.aliases?.first()?.middleName.shouldBe("Tom")
        response.data?.aliases?.first()?.lastName.shouldBe("Wick")
        response.data?.aliases?.first()?.dateOfBirth.shouldBe(LocalDate.parse("2000-02-07"))
      }

      it("returns a person without aliases when no aliases are found") {
        probationOffenderSearchApiMockServer.stubPostOffenderSearch(
          "{\"pncNumber\": \"$hmppsId\"}",
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

        val response = probationOffenderSearchGateway.getPerson(hmppsId)

        response.data?.aliases.shouldBeEmpty()
      }

      it("returns null when 400 Bad Request is returned") {
        probationOffenderSearchApiMockServer.stubPostOffenderSearch(
          "{\"pncNumber\": \"$hmppsId\"}",
          """
          {
            "developerMessage": "reason for bad request"
          }
          """,
          HttpStatus.BAD_REQUEST,
        )

        val response = probationOffenderSearchGateway.getPerson(hmppsId)
        response.data.shouldBeNull()
        response.errors.shouldBe(
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
              type = UpstreamApiError.Type.BAD_REQUEST,
            ),
          ),
        )
      }

      it("returns null when no offenders are returned") {
        probationOffenderSearchApiMockServer.stubPostOffenderSearch(
          "{\"pncNumber\": \"$hmppsId\"}",
          "[]",
        )

        val response = probationOffenderSearchGateway.getPerson(hmppsId)

        response.data.shouldBeNull()
      }
    }
    describe("when useCrnInsteadOfPncId feature flag is true") {
      val hmppsId = "X777776"

      beforeEach {
        ReflectionTestUtils.setField(probationOffenderSearchGateway, "useCrnInsteadOfPncId", true)
        probationOffenderSearchApiMockServer.stubPostOffenderSearch(
          "{\"crn\": \"$hmppsId\"}",
          """
        [
           {
            "firstName": "Jonathan",
            "middleNames": [
              "Echo",
              "Fred"
            ],
            "surname": "Bravo",
            "dateOfBirth": "1970-02-07",
            "offenderAliases": [
              {
                "dateOfBirth": "2000-02-07",
                "firstName": "John",
                "middleNames": [
                  "Tom"
                ],
                "surname": "Wick"
              }
            ]
          }
        ]
      """,
        )
      }

      it("authenticates using HMPPS Auth with credentials") {
        probationOffenderSearchGateway.getPerson(hmppsId)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("Probation Offender Search")
      }

      it("returns a person with the matching ID") {
        val response = probationOffenderSearchGateway.getPerson(hmppsId)

        response.data?.firstName.shouldBe("Jonathan")
        response.data?.middleName.shouldBe("Echo Fred")
        response.data?.lastName.shouldBe("Bravo")
        response.data?.dateOfBirth.shouldBe(LocalDate.parse("1970-02-07"))
        response.data?.aliases?.first()?.firstName.shouldBe("John")
        response.data?.aliases?.first()?.middleName.shouldBe("Tom")
        response.data?.aliases?.first()?.lastName.shouldBe("Wick")
        response.data?.aliases?.first()?.dateOfBirth.shouldBe(LocalDate.parse("2000-02-07"))
      }

      it("returns a person without aliases when no aliases are found") {
        probationOffenderSearchApiMockServer.stubPostOffenderSearch(
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

        val response = probationOffenderSearchGateway.getPerson(hmppsId)

        response.data?.aliases.shouldBeEmpty()
      }

      it("returns null when 400 Bad Request is returned") {
        probationOffenderSearchApiMockServer.stubPostOffenderSearch(
          "{\"crn\": \"$hmppsId\"}",
          """
          {
            "developerMessage": "reason for bad request"
          }
          """,
          HttpStatus.BAD_REQUEST,
        )

        val response = probationOffenderSearchGateway.getPerson(hmppsId)
        response.data.shouldBeNull()
        response.errors.shouldBe(
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
              type = UpstreamApiError.Type.BAD_REQUEST,
            ),
          ),
        )
      }

      it("returns null when no offenders are returned") {
        probationOffenderSearchApiMockServer.stubPostOffenderSearch(
          "{\"crn\": \"$hmppsId\"}",
          "[]",
        )

        val response = probationOffenderSearchGateway.getPerson(hmppsId)

        response.data.shouldBeNull()
      }
    }
  }
},)
