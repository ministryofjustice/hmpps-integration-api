package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
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
      val personNomisOnly =
        Person(
          firstName = "Chandler",
          lastName = "ProbationBing",
          identifiers = Identifiers(nomisNumber = nomisNumber),
        )
      val personDeliusOnly =
        Person(
          firstName = "Chandler",
          lastName = "ProbationBing",
          identifiers = Identifiers(deliusCrn = nDeliusCRN),
        )
      val personNoIdentifiers =
        Person(firstName = "Qui-gon", lastName = "Jin")
      val nomisSentence1 = generateTestSentence()
      val nomisSentence2 = generateTestSentence()
      val nDeliusSentence1 = generateTestSentence()
      val nDeliusSentence2 = generateTestSentence()

      val nomis404Error =
        listOf(
          UpstreamApiError(
            type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            causedBy = UpstreamApi.NOMIS,
          ),
        )
      val delius404Error =
        listOf(
          UpstreamApiError(
            type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            causedBy = UpstreamApi.NDELIUS,
          ),
        )
      val probationOffenderSearch500Error =
        listOf(
          UpstreamApiError(
            type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
            causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
            description = "Mock error from person service",
          ),
        )
      beforeEach {
        Mockito.reset(nomisGateway)
        Mockito.reset(nDeliusGateway)
        Mockito.reset(getPersonService)
      }

      it("Person service error → Return person service error") {
        val errors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
              causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
              description = "Mock error from person service",
            ),
          )
        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )
        val result = getSentencesForPersonService.execute(hmppsId)

        result.errors.shouldBe(errors)
      }

      it("No Nomis number + no Delius crn -> Return entity not found response") {
        val errors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
            ),
          )
        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = personNoIdentifiers,
          ),
        )
        val result = getSentencesForPersonService.execute(hmppsId)

        result.errors.shouldBe(errors)
      }

      it("No Nomis number + Delius crn, delius success → return Delius") {

        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = personDeliusOnly,
          ),
        )
        whenever(nDeliusGateway.getSentencesForPerson(nDeliusCRN)).thenReturn(
          Response(data = listOf(nDeliusSentence1, nDeliusSentence2)),
        )
        val result = getSentencesForPersonService.execute(hmppsId)
        result.shouldBe(Response(data = listOf(nDeliusSentence1, nDeliusSentence2)))
      }

      it("No Nomis number + Delius crn, delius any error → return Delius error") {
        val errors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
              causedBy = UpstreamApi.NDELIUS,
            ),
          )
        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = personDeliusOnly,
          ),
        )
        whenever(nDeliusGateway.getSentencesForPerson(nDeliusCRN)).thenReturn(
          Response(data = emptyList(), errors = errors),
        )
        val result = getSentencesForPersonService.execute(hmppsId)
        result.errors.shouldBe(errors)
      }

      it("Nomis number + No Delius crn, Nomis success -> Return Nomis") {
        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = personNomisOnly,
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

        val result = getSentencesForPersonService.execute(hmppsId)
        result.shouldBe(Response(data = listOf(nomisSentence1)))
      }

      it("Nomis number + No Delius crn, Nomis any error on bookings-> Return Nomis error") {

        val errors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
              causedBy = UpstreamApi.NOMIS,
            ),
          )
        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = personNomisOnly,
          ),
        )

        whenever(nomisGateway.getBookingIdsForPerson(nomisNumber)).thenReturn(
          Response(
            data = emptyList(),
            errors = errors,
          ),
        )

        val result = getSentencesForPersonService.execute(hmppsId)
        result.errors.shouldBe(errors)
      }

      it("Nomis number + No Delius crn, Nomis any error on sentences -> Return Nomis error") {
        val errors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
              causedBy = UpstreamApi.NOMIS,
            ),
          )

        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = personNomisOnly,
          ),
        )

        whenever(nomisGateway.getBookingIdsForPerson(nomisNumber)).thenReturn(
          Response(
            data = listOf(NomisBooking(bookingId = firstBookingId), NomisBooking(bookingId = secondBookingId)),
          ),
        )

        whenever(nomisGateway.getSentencesForBooking(firstBookingId)).thenReturn(
          Response(data = emptyList(), errors = errors),
        )

        val result = getSentencesForPersonService.execute(hmppsId)
        result.errors.shouldBe(errors)
      }

      it("Nomis number + Delius crn, Nomis success, Delius success → Merge responses") {
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
          Response(data = listOf(nomisSentence2)),
        )
        whenever(nomisGateway.getSentencesForBooking(secondBookingId)).thenReturn(
          Response(data = listOf(nomisSentence2)),
        )

        whenever(nDeliusGateway.getSentencesForPerson(nDeliusCRN)).thenReturn(
          Response(data = listOf(nDeliusSentence1, nDeliusSentence2)),
        )

        val result = getSentencesForPersonService.execute(hmppsId)
        result.shouldBe(Response(data = listOf(nomisSentence1, nomisSentence2, nDeliusSentence1, nDeliusSentence2)))
      }

      it("Nomis number + Delius crn, Nomis success, Delius 404 → Return Nomis") {
        val errors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              causedBy = UpstreamApi.NDELIUS,
            ),
          )

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
          Response(data = emptyList(), errors = errors),
        )

        val result = getSentencesForPersonService.execute(hmppsId)
        result.shouldBe(Response(data = listOf(nomisSentence1, nomisSentence2)))
      }

      it("Nomis number + Delius crn, Nomis 404 on booking ids, Delius success → Return Delius") {
        val errors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              causedBy = UpstreamApi.NOMIS,
            ),
          )

        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = personFromProbationOffenderSearch,
          ),
        )

        whenever(nomisGateway.getBookingIdsForPerson(nomisNumber)).thenReturn(
          Response(
            data = emptyList(),
            errors = errors,
          ),
        )

        whenever(nDeliusGateway.getSentencesForPerson(nDeliusCRN)).thenReturn(
          Response(data = listOf(nDeliusSentence1, nDeliusSentence2)),
        )

        val result = getSentencesForPersonService.execute(hmppsId)
        result.shouldBe(Response(data = listOf(nDeliusSentence1, nDeliusSentence2)))
      }

      it("Nomis number + Delius crn, Nomis 404 on sentences, Delius success → Return Delius") {
        val errors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              causedBy = UpstreamApi.NOMIS,
            ),
          )

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
          Response(data = emptyList(), errors = errors),
        )

        whenever(nDeliusGateway.getSentencesForPerson(nDeliusCRN)).thenReturn(
          Response(data = listOf(nDeliusSentence1, nDeliusSentence2)),
        )

        val result = getSentencesForPersonService.execute(hmppsId)
        result.shouldBe(Response(data = listOf(nDeliusSentence1, nDeliusSentence2)))
      }

      it("Nomis number + Delius crn, Nomis non 404 error on booking ids-> Return Nomis error") {
        val errors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
              causedBy = UpstreamApi.NOMIS,
            ),
          )

        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = personFromProbationOffenderSearch,
          ),
        )

        whenever(nomisGateway.getBookingIdsForPerson(nomisNumber)).thenReturn(
          Response(
            data = emptyList(),
            errors = errors,
          ),
        )

        val result = getSentencesForPersonService.execute(hmppsId)
        result.errors.shouldBe(errors)
      }

      it("Nomis number + Delius crn, Nomis non 404 error on sentences -> Return Nomis error") {
        val errors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
              causedBy = UpstreamApi.NOMIS,
            ),
          )

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
          Response(data = emptyList(), errors = errors),
        )

        val result = getSentencesForPersonService.execute(hmppsId)
        result.errors.shouldBe(errors)
      }

      it("Nomis number + Delius crn, Delius non 404 error -> Return Delius error") {
        val errors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
              causedBy = UpstreamApi.NOMIS,
            ),
          )

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
          Response(data = emptyList(), errors = errors),
        )

        val result = getSentencesForPersonService.execute(hmppsId)
        result.errors.shouldBe(errors)
      }

      it("Nomis number + Delius crn, Nomis 404 on booking ids, Delius any error -> Return Delius error") {
        val nomisErrors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              causedBy = UpstreamApi.NOMIS,
            ),
          )
        val deliusErrors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
              causedBy = UpstreamApi.NDELIUS,
            ),
          )

        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = personFromProbationOffenderSearch,
          ),
        )

        whenever(nomisGateway.getBookingIdsForPerson(nomisNumber)).thenReturn(
          Response(
            data = emptyList(),
            errors = nomisErrors,
          ),
        )

        whenever(nDeliusGateway.getSentencesForPerson(nDeliusCRN)).thenReturn(
          Response(data = emptyList(), errors = deliusErrors),
        )

        val result = getSentencesForPersonService.execute(hmppsId)
        result.errors.shouldBe(deliusErrors)
      }

      it("Nomis number + Delius crn, Nomis 404 on sentences, Delius any error -> Return Delius error") {
        val nomisErrors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              causedBy = UpstreamApi.NOMIS,
            ),
          )
        val deliusErrors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
              causedBy = UpstreamApi.NDELIUS,
            ),
          )

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
          Response(data = emptyList(), errors = nomisErrors),
        )

        whenever(nDeliusGateway.getSentencesForPerson(nDeliusCRN)).thenReturn(
          Response(data = emptyList(), errors = deliusErrors),
        )

        val result = getSentencesForPersonService.execute(hmppsId)
        result.errors.shouldBe(deliusErrors)
      }

      it("Nomis number + Delius crn, Nomis any error on booking ids, Delius 404 -> Return Nomis error") {
        val nomisErrors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              causedBy = UpstreamApi.NOMIS,
            ),
          )
        val deliusErrors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              causedBy = UpstreamApi.NDELIUS,
            ),
          )

        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = personFromProbationOffenderSearch,
          ),
        )

        whenever(nomisGateway.getBookingIdsForPerson(nomisNumber)).thenReturn(
          Response(
            data = emptyList(),
            errors = nomisErrors,
          ),
        )

        whenever(nDeliusGateway.getSentencesForPerson(nDeliusCRN)).thenReturn(
          Response(data = emptyList(), errors = deliusErrors),
        )

        val result = getSentencesForPersonService.execute(hmppsId)
        result.errors.shouldBe(nomisErrors)
      }

      it("Nomis number + Delius crn, Nomis any error on sentences, Delius 404 -> Return Nomis error") {
        val nomisErrors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              causedBy = UpstreamApi.NOMIS,
            ),
          )
        val deliusErrors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              causedBy = UpstreamApi.NDELIUS,
            ),
          )

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
          Response(data = emptyList(), errors = nomisErrors),
        )

        whenever(nDeliusGateway.getSentencesForPerson(nDeliusCRN)).thenReturn(
          Response(data = emptyList(), errors = deliusErrors),
        )

        val result = getSentencesForPersonService.execute(hmppsId)
        result.errors.shouldBe(nomisErrors)
      }

//      describe("Find person by hmppsId") {
//        it("gets a person from getPersonService") {
//          getSentencesForPersonService.execute(hmppsId)
//
//          verify(getPersonService, VerificationModeFactory.times(1)).execute(hmppsId = hmppsId)
//        }
//
//        it("returns prison and probation sentences") {
//          val result = getSentencesForPersonService.execute(hmppsId)
//
//          result.shouldBe(Response(data = listOf(nomisSentence1, nomisSentence2, nDeliusSentence1, nDeliusSentence2)))
//        }
//
//        it("returns no sentences when the person cannot be found in either Prisoner Offender Search or Probation Offender Search") {
//          whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
//            Response(data = null),
//          )
//
//          val result = getSentencesForPersonService.execute(hmppsId)
//
//          result.shouldBe(Response(data = emptyList()))
//        }
//      }
//
//      describe("Nomis sentences") {
//        it("gets bookind Ids for a person from Nomis using a Nomis number") {
//          getSentencesForPersonService.execute(hmppsId)
//
//          verify(nomisGateway, VerificationModeFactory.times(1)).getBookingIdsForPerson(id = nomisNumber)
//        }
//
//        it("does not return prison sentences when booking IDs are not present") {
//          whenever(nomisGateway.getBookingIdsForPerson(nomisNumber)).thenReturn(Response(data = emptyList()))
//
//          val result = getSentencesForPersonService.execute(hmppsId)
//          result.data.shouldNotContain(listOf(nomisSentence1, nomisSentence2))
//        }
//
//        it("gets all sentences from Nomis with booking IDs") {
//          getSentencesForPersonService.execute(hmppsId)
//
//          verify(nomisGateway, VerificationModeFactory.times(1)).getSentencesForBooking(firstBookingId)
//          verify(nomisGateway, VerificationModeFactory.times(1)).getSentencesForBooking(secondBookingId)
//        }
//      }
//
//      describe("NDelius sentence") {
//        it("gets all sentences from nDelius using a CRN") {
//          getSentencesForPersonService.execute(hmppsId)
//
//          verify(nDeliusGateway, VerificationModeFactory.times(1)).getSentencesForPerson(nDeliusCRN)
//        }
//      }
//
//      it("combines and returns sentences from Nomis and nDelius") {
//        val response = getSentencesForPersonService.execute(hmppsId)
//
//        response.data.shouldBe(
//          listOf(
//            nomisSentence1,
//            nomisSentence2,
//            nDeliusSentence1,
//            nDeliusSentence2,
//          ),
//        )
//      }
//
//      it("returns an empty list when no sentences were found in Nomis or nDelius") {
//        whenever(nDeliusGateway.getSentencesForPerson(nDeliusCRN)).thenReturn(Response(data = emptyList()))
//        whenever(nomisGateway.getBookingIdsForPerson(nomisNumber)).thenReturn(Response(data = emptyList()))
//
//        val response = getSentencesForPersonService.execute(hmppsId)
//
//        response.data.shouldBe(emptyList())
//      }
//
//      describe("upstream API errors") {
//        it("returns errors from Nomis getBookingIdsForPerson") {
//          whenever(nomisGateway.getBookingIdsForPerson(nomisNumber)).thenReturn(
//            Response(
//              data = emptyList(),
//              errors =
//                listOf(
//                  UpstreamApiError(
//                    causedBy = UpstreamApi.NOMIS,
//                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
//                  ),
//                ),
//            ),
//          )
//
//          val response = getSentencesForPersonService.execute(hmppsId)
//          response.errors.shouldHaveSize(1)
//        }
//
//        it("returns errors from Nomis getSentencesForBooking") {
//          whenever(nomisGateway.getSentencesForBooking(firstBookingId)).thenReturn(
//            Response(
//              data = emptyList(),
//              errors =
//                listOf(
//                  UpstreamApiError(
//                    causedBy = UpstreamApi.NOMIS,
//                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
//                  ),
//                ),
//            ),
//          )
//
//          val response = getSentencesForPersonService.execute(hmppsId)
//          response.errors.shouldHaveSize(1)
//        }
//
//        it("returns errors from nDelius getSentencesForPerson") {
//          whenever(nDeliusGateway.getSentencesForPerson(nDeliusCRN)).thenReturn(
//            Response(
//              data = emptyList(),
//              errors =
//                listOf(
//                  UpstreamApiError(
//                    causedBy = UpstreamApi.NDELIUS,
//                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
//                  ),
//                ),
//            ),
//          )
//
//          val response = getSentencesForPersonService.execute(hmppsId)
//          response.errors.shouldHaveSize(1)
//        }
//      }
    },
  )
