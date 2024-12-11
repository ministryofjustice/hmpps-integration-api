package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.generateTestSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisBooking

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetSentencesForPersonService::class],
)
internal class GetSentencesForPersonServiceTest(
  @MockitoBean val nomisGateway: NomisGateway,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val nDeliusGateway: NDeliusGateway,
  private val getSentencesForPersonService: GetSentencesForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "1234/56789B"
      val nomisNumber = "Z99999ZZ"
      val nDeliusCRN = "X123456"
      val firstBookingId = 1
      val secondBookingId = 2
      val personFromProbationOffenderSearch =
        Person(
          firstName = "Chandler",
          lastName = "ProbationBing",
          identifiers = Identifiers(deliusCrn = nDeliusCRN, nomisNumber = nomisNumber),
        )
      val nomisSentence1 = generateTestSentence()
      val nomisSentence2 = generateTestSentence()
      val nDeliusSentence1 = generateTestSentence()
      val nDeliusSentence2 = generateTestSentence()

      beforeEach {
        Mockito.reset(nomisGateway)
        Mockito.reset(nDeliusGateway)
        Mockito.reset(getPersonService)

        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = personFromProbationOffenderSearch,
          ),
        )

        whenever(nomisGateway.getBookingIdsForPerson(nomisNumber)).thenReturn(
          Response(
            data = listOf(NomisBooking(bookingId = firstBookingId), NomisBooking(bookingId = secondBookingId)),
          ),
        )

        whenever(nomisGateway.getSentencesForBooking(firstBookingId)).thenReturn(
          Response(data = listOf(nomisSentence1)),
        )

        whenever(nomisGateway.getSentencesForBooking(secondBookingId)).thenReturn(
          Response(data = listOf(nomisSentence2)),
        )

        whenever(nDeliusGateway.getSentencesForPerson(nDeliusCRN)).thenReturn(
          Response(data = listOf(nDeliusSentence1, nDeliusSentence2)),
        )
      }

      describe("Find person by hmppsId") {
        it("gets a person from getPersonService") {
          getSentencesForPersonService.execute(hmppsId)

          verify(getPersonService, VerificationModeFactory.times(1)).execute(hmppsId = hmppsId)
        }

        it("returns prison and probation sentences") {
          val result = getSentencesForPersonService.execute(hmppsId)

          result.shouldBe(Response(data = listOf(nomisSentence1, nomisSentence2, nDeliusSentence1, nDeliusSentence2)))
        }

        it("returns no sentences when the person cannot be found in either Prisoner Offender Search or Probation Offender Search") {
          whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
            Response(data = null),
          )

          val result = getSentencesForPersonService.execute(hmppsId)

          result.shouldBe(Response(data = emptyList()))
        }
      }

      describe("Nomis sentences") {
        it("gets bookind Ids for a person from Nomis using a Nomis number") {
          getSentencesForPersonService.execute(hmppsId)

          verify(nomisGateway, VerificationModeFactory.times(1)).getBookingIdsForPerson(id = nomisNumber)
        }

        it("does not return prison sentences when booking IDs are not present") {
          whenever(nomisGateway.getBookingIdsForPerson(nomisNumber)).thenReturn(Response(data = emptyList()))

          val result = getSentencesForPersonService.execute(hmppsId)
          result.data.shouldNotContain(listOf(nomisSentence1, nomisSentence2))
        }

        it("gets all sentences from Nomis with booking IDs") {
          getSentencesForPersonService.execute(hmppsId)

          verify(nomisGateway, VerificationModeFactory.times(1)).getSentencesForBooking(firstBookingId)
          verify(nomisGateway, VerificationModeFactory.times(1)).getSentencesForBooking(secondBookingId)
        }
      }

      describe("NDelius sentence") {
        it("gets all sentences from nDelius using a CRN") {
          getSentencesForPersonService.execute(hmppsId)

          verify(nDeliusGateway, VerificationModeFactory.times(1)).getSentencesForPerson(nDeliusCRN)
        }
      }

      it("combines and returns sentences from Nomis and nDelius") {
        val response = getSentencesForPersonService.execute(hmppsId)

        response.data.shouldBe(
          listOf(
            nomisSentence1,
            nomisSentence2,
            nDeliusSentence1,
            nDeliusSentence2,
          ),
        )
      }

      it("returns an empty list when no sentences were found in Nomis or nDelius") {
        whenever(nDeliusGateway.getSentencesForPerson(nDeliusCRN)).thenReturn(Response(data = emptyList()))
        whenever(nomisGateway.getBookingIdsForPerson(nomisNumber)).thenReturn(Response(data = emptyList()))

        val response = getSentencesForPersonService.execute(hmppsId)

        response.data.shouldBe(emptyList())
      }

      describe("upstream API errors") {
        it("returns errors from Nomis getBookingIdsForPerson") {
          whenever(nomisGateway.getBookingIdsForPerson(nomisNumber)).thenReturn(
            Response(
              data = emptyList(),
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NOMIS,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val response = getSentencesForPersonService.execute(hmppsId)
          response.errors.shouldHaveSize(1)
        }

        it("returns errors from Nomis getSentencesForBooking") {
          whenever(nomisGateway.getSentencesForBooking(firstBookingId)).thenReturn(
            Response(
              data = emptyList(),
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NOMIS,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val response = getSentencesForPersonService.execute(hmppsId)
          response.errors.shouldHaveSize(1)
        }

        it("returns errors from nDelius getSentencesForPerson") {
          whenever(nDeliusGateway.getSentencesForPerson(nDeliusCRN)).thenReturn(
            Response(
              data = emptyList(),
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NDELIUS,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val response = getSentencesForPersonService.execute(hmppsId)
          response.errors.shouldHaveSize(1)
        }
      }
    },
  )
