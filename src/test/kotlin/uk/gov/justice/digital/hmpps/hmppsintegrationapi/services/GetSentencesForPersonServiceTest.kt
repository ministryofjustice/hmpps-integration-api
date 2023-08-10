package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
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
  @MockBean val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @MockBean val nDeliusGateway: NDeliusGateway,
  private val getSentencesForPersonService: GetSentencesForPersonService,
) : DescribeSpec(
  {
    val pncId = "1234/56789B"
    val prisonerNumber = "Z99999ZZ"
    val nDeliusCRN = "X123456"
    val firstBookingId = 1
    val secondBookingId = 2

    beforeEach {
      Mockito.reset(nomisGateway)
      Mockito.reset(nDeliusGateway)
      Mockito.reset(prisonerOffenderSearchGateway)
      Mockito.reset(probationOffenderSearchGateway)

      whenever(prisonerOffenderSearchGateway.getPersons(pncId = pncId)).thenReturn(
        Response(data = listOf(Person(firstName = "Chandler", lastName = "Bing", identifiers = Identifiers(nomisNumber = prisonerNumber)))),
      )

      whenever(probationOffenderSearchGateway.getPerson(pncId = pncId)).thenReturn(
        Response(data = Person(firstName = "Chandler", lastName = "ProbationBing", identifiers = Identifiers(deliusCrn = nDeliusCRN))),
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
            Sentence(
              dateOfSentencing = LocalDate.parse("2001-01-01"),
            ),
          ),
        ),
      )

      whenever(nomisGateway.getSentencesForBooking(secondBookingId)).thenReturn(
        Response(
          data = listOf(
            Sentence(
              dateOfSentencing = LocalDate.parse("2002-01-01"),
            ),
          ),
        ),
      )

      whenever(nDeliusGateway.getSentencesForPerson(nDeliusCRN)).thenReturn(
        Response(
          data = listOf(
            Sentence(
              dateOfSentencing = LocalDate.parse("2003-01-01"),
            ),
            Sentence(
              dateOfSentencing = LocalDate.parse("2004-01-01"),
            ),
          ),
        ),
      )
    }

    describe("probation and prisoner offender search") {
      it("retrieves prisoner ID from Prisoner Offender Search using a PNC ID") {
        getSentencesForPersonService.execute(pncId)

        verify(prisonerOffenderSearchGateway, VerificationModeFactory.times(1)).getPersons(pncId = pncId)
      }

      it("retrieves nDelius CRN from Probation Offender Search using a PNC ID") {
        getSentencesForPersonService.execute(pncId)

        verify(probationOffenderSearchGateway, VerificationModeFactory.times(1)).getPerson(pncId = pncId)
      }

      it("returns errors when a person cannot be found by PNC ID in prisoner offender search") {
        whenever(prisonerOffenderSearchGateway.getPersons(pncId = pncId)).thenReturn(
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

      it("returns errors when a person cannot be found by a PNC ID in probation offender search") {
        whenever(probationOffenderSearchGateway.getPerson(pncId = pncId)).thenReturn(
          Response(
            data = null,
            errors = listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.NDELIUS,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
          ),
        )

        val response = getSentencesForPersonService.execute(pncId)

        response.errors.shouldHaveSize(1)
      }
    }

    describe("Nomis sentence search") {
      it("retrieves bookind Ids for a person from Nomis using a Nomis number") {
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

      it("retrieves all sentences from Nomis for a PNC Id") {
        getSentencesForPersonService.execute(pncId)

        verify(nomisGateway, VerificationModeFactory.times(1)).getSentencesForBooking(firstBookingId)
        verify(nomisGateway, VerificationModeFactory.times(1)).getSentencesForBooking(secondBookingId)
      }

      it("records an error when no sentence was found in Nomis for a Booking Id") {
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
    }

    describe("NDelius sentence search") {
      it("retrieves sentences from nDelius using a CRN") {
        getSentencesForPersonService.execute(pncId)

        verify(nDeliusGateway, VerificationModeFactory.times(1)).getSentencesForPerson(nDeliusCRN)
      }

      it("records an error when no sentence was found for a nDeliusCRN") {
        whenever(nDeliusGateway.getSentencesForPerson(nDeliusCRN)).thenReturn(
          Response(
            data = emptyList(),
            errors = listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.NDELIUS,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
          ),
        )

        val response = getSentencesForPersonService.execute(pncId)

        response.errors.shouldHaveSize(1)
      }
    }

    it("combines and returns sentences from Nomis and nDelius") {
      val response = getSentencesForPersonService.execute(pncId)

      response.data.shouldBe(
        listOf(
          Sentence(dateOfSentencing = LocalDate.parse("2001-01-01")),
          Sentence(dateOfSentencing = LocalDate.parse("2002-01-01")),
          Sentence(dateOfSentencing = LocalDate.parse("2003-01-01")),
          Sentence(dateOfSentencing = LocalDate.parse("2004-01-01")),
        ),
      )
    }

    it("returns an empty list when no sentences were found in Nomis and nDelius") {
      whenever(nDeliusGateway.getSentencesForPerson(nDeliusCRN)).thenReturn(
        Response(
          data = emptyList(),
          errors = listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.NDELIUS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          ),
        ),
      )

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

      response.data.shouldBe(emptyList())
    }
  },
)
