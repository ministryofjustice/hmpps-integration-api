package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.Booking
import java.time.LocalDate

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetSentencesForPersonService::class],
)
internal class GetSentencesForPersonServiceTest(
  @MockBean val nomisGateway: NomisGateway,
  @MockBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  private val getSentencesForPersonService: GetSentencesForPersonService,
) : DescribeSpec(
  {
    val pncId = "1234/56789B"
    val prisonerNumber = "Z99999ZZ"
    val firstBookingId = 1
    val secondBookingId = 2

    beforeEach {
      Mockito.reset(nomisGateway)
      Mockito.reset(prisonerOffenderSearchGateway)

      whenever(prisonerOffenderSearchGateway.getPersons(pncId = pncId)).thenReturn(
        Response(
          data = listOf(
            Person(
              firstName = "Chandler",
              lastName = "Bing",
              identifiers = Identifiers(nomisNumber = prisonerNumber),
            ),
          ),
        ),
      )

      whenever(nomisGateway.getBookingIdsForPerson(prisonerNumber)).thenReturn(
        Response(
          data = listOf(
            Booking(
              bookingId = firstBookingId,
            ),
            Booking(
              bookingId = secondBookingId,
            ),
          ),
        ),
      )

      whenever(nomisGateway.getSentencesForBooking(firstBookingId)).thenReturn(
        Response(
          data = listOf(
            Sentence(startDate = LocalDate.parse("2001-01-01")),
          ),
        ),
      )

      whenever(nomisGateway.getSentencesForBooking(secondBookingId)).thenReturn(
        Response(
          data = listOf(
            Sentence(startDate = LocalDate.parse("2002-01-01")),
          ),
        ),
      )
    }

    it("calls #getPersons on Prisoner Offender Search with a PNC Id") {
      getSentencesForPersonService.execute(pncId)

      verify(prisonerOffenderSearchGateway, VerificationModeFactory.times(1)).getPersons(pncId = pncId)
    }

    it("records an error when it fails to retrieve a Nomis number from Prisoner Offender Search using a PNC ID") {
      whenever(prisonerOffenderSearchGateway.getPersons(pncId = pncId)).thenReturn(
        Response(
          data = emptyList(),
          errors = listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          ),
        ),
      )

      val response = getSentencesForPersonService.execute(pncId)!!

      response.errors.shouldHaveSize(1)
    }

    it("calls #getBookingIdsForPerson on Nomis with a Nomis number") {
      getSentencesForPersonService.execute(pncId)

      verify(nomisGateway, VerificationModeFactory.times(1)).getBookingIdsForPerson(id = prisonerNumber)
    }

    it("records errors when no booking Ids are found for a Nomis number") {
      whenever(nomisGateway.getBookingIdsForPerson(prisonerNumber)).thenReturn(
        Response(
          data = emptyList(),
          errors = listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.NOMIS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          ),
        ),
      )

      val response = getSentencesForPersonService.execute(pncId)!!

      response.errors.shouldHaveSize(1)
    }

    it("it calls #getSentenceForBookingId and returns all sentences from Nomis for a PNC Id") {
      val response = getSentencesForPersonService.execute(pncId)

      verify(nomisGateway, VerificationModeFactory.times(1)).getSentencesForBooking(firstBookingId)
      verify(nomisGateway, VerificationModeFactory.times(1)).getSentencesForBooking(secondBookingId)

      response.data.shouldBeEqual(
        listOf(
          Sentence(startDate = LocalDate.parse("2001-01-01")),
          Sentence(startDate = LocalDate.parse("2002-01-01")),
        ),
      )
    }

    it("records an error when no sentence was found for a Booking Id") {
      whenever(nomisGateway.getBookingIdsForPerson(prisonerNumber)).thenReturn(
        Response(
          data = emptyList(),
          errors = listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.NOMIS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          ),
        ),
      )

      val response = getSentencesForPersonService.execute(pncId)

      response.errors.shouldHaveSize(1)
    }
  },
)
