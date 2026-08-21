package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.RESTAPICLIENT_FOR_PRISON_API_GATEWAY
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.generateTestSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceLength
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceTerm
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiSentence
import java.time.LocalDate

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonApiGateway::class],
)
class GetSentencesForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  val prisonApiGateway: PrisonApiGateway,
) : DescribeSpec(
    {
      val nomisApiMockServer = ApiMockServer.create(UpstreamApi.PRISON_API)
      val offenderNo = "zyx987"
      val someBookingId = 1
      val sentecesAndOffencesPath = "/api/offender-sentences/booking/$someBookingId/sentences-and-offences"
      var sentencesPath = "/api/offender-sentences?offenderNo=$offenderNo"
      beforeEach {
        nomisApiMockServer.start()
        nomisApiMockServer.stubForGet(
          sentencesPath,
          """
          [
            {
              "bookingId": 1,
            },
            {
              "bookingId": 2
            }
          ]
        """.removeWhitespaceAndNewlines(),
        )

        nomisApiMockServer.stubForGet(
          sentecesAndOffencesPath,
          """
          {
            "fineAmount": "40",
            "sentenceDate": "2001-01-01",
            "sentenceStatus": "A",
            "sentenceTypeDescription": "ORA CJA03 Standard Determinate Sentence",
            "terms": [
                {
                  "years": 1,
                  "months": 2,
                  "weeks": 3,
                  "days": 4
                }
              ]
          }
        """.removeWhitespaceAndNewlines(),
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        nomisApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        prisonApiGateway.getSentencesForBooking(someBookingId)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
      }

      it("returns a sentence for a matching bookingId") {
        val response = prisonApiGateway.getSentencesForBooking(someBookingId)

        response.data.shouldBe(
          listOf(
            generateTestSentence(
              dateOfSentencing = LocalDate.parse("2001-01-01"),
              description = "ORA CJA03 Standard Determinate Sentence",
              fineAmount = 40,
              isActive = true,
              isCustodial = true,
              length =
                SentenceLength(
                  terms =
                    listOf(
                      SentenceTerm(
                        years = 1,
                        months = 2,
                        weeks = 3,
                        days = 4,
                      ),
                    ),
                ),
            ),
          ),
        )
      }

      it("returns an error when 404 Not Found is returned because no person is found") {
        nomisApiMockServer.stubForGet(sentencesPath, "", HttpStatus.NOT_FOUND)

        val response = prisonApiGateway.getBookingIdsForPerson(offenderNo)

        response.errors.shouldHaveSize(1)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.PRISON_API)
        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }

      it("returns an error when no sentence is found") {
        nomisApiMockServer.stubForGet(sentecesAndOffencesPath, "", HttpStatus.NOT_FOUND)

        val response = prisonApiGateway.getSentencesForBooking(someBookingId)

        response.data.shouldBeEmpty()
        response.errors.shouldHaveSize(1)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.PRISON_API)
        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }

      it("can use the RestApiClient") {
        // Given
        val authToken = "ABC123"
        val headers = mapOf("Authorization" to "Bearer $authToken")

        val features = FeatureFlagConfig(mapOf(RESTAPICLIENT_FOR_PRISON_API_GATEWAY to true))

        val authGateway: HmppsAuthGateway = mock()
        whenever(authGateway.getClientToken("NOMIS", null)).thenReturn(authToken)

        val apiClient: RestApiClient = mock()
        whenever(apiClient.get(eq(sentecesAndOffencesPath), eq(Array<PrisonApiSentence>::class), eq(headers), isNull())).thenReturn(
          RestApiResponse(
            "Test",
            HttpStatus.OK,
            arrayOf(
              PrisonApiSentence(
                fineAmount = 1,
              ),
            ),
          ),
        )

        val gateway = PrisonApiGateway("http://localhost", features, apiClient)
        gateway.hmppsAuthGateway = authGateway

        // When
        val response = gateway.getSentencesForBooking(someBookingId)

        // Then
        response.data.shouldNotBeNull()
      }
    },
  )
