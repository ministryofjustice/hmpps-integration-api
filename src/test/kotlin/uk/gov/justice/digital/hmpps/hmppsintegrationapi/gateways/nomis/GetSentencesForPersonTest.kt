package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.generateTestSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NomisApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceLength
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceTerm
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.time.LocalDate

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NomisGateway::class],
)
class GetSentencesForPersonTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  val nomisGateway: NomisGateway,
) :
  DescribeSpec(
      {
        val nomisApiMockServer = NomisApiMockServer()
        val offenderNo = "zyx987"
        val someBookingId = 1
        val sentecesAndOffencesPath = "/api/offender-sentences/booking/$someBookingId/sentences-and-offences"
        var sentencesPath = "/api/offender-sentences?offenderNo=$offenderNo"
        beforeEach {
          nomisApiMockServer.start()
          nomisApiMockServer.stubNomisApiResponse(
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

          nomisApiMockServer.stubNomisApiResponse(
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
          nomisGateway.getSentencesForBooking(someBookingId)

          verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
        }

        it("returns a sentence for a matching bookingId") {
          val response = nomisGateway.getSentencesForBooking(someBookingId)

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
          nomisApiMockServer.stubNomisApiResponse(sentencesPath, "", HttpStatus.NOT_FOUND)

          val response = nomisGateway.getBookingIdsForPerson(offenderNo)

          response.errors.shouldHaveSize(1)
          response.errors.first().causedBy.shouldBe(UpstreamApi.NOMIS)
          response.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
        }

        it("returns an error when no sentence is found") {
          nomisApiMockServer.stubNomisApiResponse(sentecesAndOffencesPath, "", HttpStatus.NOT_FOUND)

          val response = nomisGateway.getSentencesForBooking(someBookingId)

          response.data.shouldBeEmpty()
          response.errors.shouldHaveSize(1)
          response.errors.first().causedBy.shouldBe(UpstreamApi.NOMIS)
          response.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
        }
      },
    )
