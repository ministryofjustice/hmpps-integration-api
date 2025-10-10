package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.ContactEventHelper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.MappaCategory

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ContactEventService::class],
)
internal class ContactEventServiceTest(
  @MockitoBean val prisonApiGateway: PrisonApiGateway,
  @MockitoBean val personService: GetPersonService,
  @MockitoBean val deliusGateway: NDeliusGateway,
  private val contactEventService: ContactEventService,
) : DescribeSpec(
    {
      val persona = personInProbationAndNomisPersona
      val hmppsId = persona.identifiers.deliusCrn!!
      val crn = hmppsId
      val person = Person(firstName = persona.firstName, lastName = persona.lastName, identifiers = persona.identifiers)
      val contactEvents = ContactEventHelper.generateNDeliusContactEvents(crn, 10, 1, 100)
      val mappaCategories = emptyList<Number>()
      val mappaCats = emptyList<MappaCategory>()
      val filters = ConsumerFilters(mappaCategories = mappaCats)

      beforeEach {

        Mockito.reset(prisonApiGateway)
        Mockito.reset(personService)
        Mockito.reset(deliusGateway)

        whenever(personService.execute(hmppsId = hmppsId)).thenReturn(Response(person))
        whenever(deliusGateway.getContactEventsForPerson(crn, 1, 10, mappaCategories))
          .thenReturn(Response(contactEvents))

        whenever(deliusGateway.getContactEventForPerson(crn, 1, mappaCategories))
          .thenReturn(Response(contactEvents.content.first()))
      }

      it("contact events calls personService search with hmppsId") {
        contactEventService.getContactEvents(hmppsId, 1, 10, filters)
        verify(personService, times(1)).execute(hmppsId = hmppsId)
      }

      it("contact events should return a list of errors if person not found") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.CVL,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )
        whenever(personService.execute(hmppsId = "notfound")).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )
        val exception =
          assertThrows<EntityNotFoundException> {
            contactEventService.getContactEvents("notfound", 1, 10, filters)
          }
        exception.message.shouldBe("NDelius CRN not found for notfound")
      }

      it("contact events should return a list of errors if ndelius gateway service returns error") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.NDELIUS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )

        whenever(deliusGateway.getContactEventsForPerson(crn, 1, 10, mappaCategories)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = contactEventService.getContactEvents(crn, 1, 10, filters)
        result.data?.content.shouldBe(null)
        result.errors.shouldBe(errors)
      }

      it("contact events should return contact events from gateway") {
        val result = contactEventService.getContactEvents(crn, 1, 10, filters)
        result.data?.content.shouldBe(contactEvents.content.map { it.toContactEvent() })
        result.errors.count().shouldBe(0)
      }

      it("contact event calls personService search with hmppsId") {
        contactEventService.getContactEvent(hmppsId, 1, filters)
        verify(personService, times(1)).execute(hmppsId = hmppsId)
      }

      it("contact event should return a list of errors if person not found") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.NDELIUS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )
        whenever(personService.execute(hmppsId = "notfound")).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )
        val exception =
          assertThrows<EntityNotFoundException> {
            contactEventService.getContactEvent("notfound", 1, filters)
          }
        exception.message.shouldBe("NDelius CRN not found for notfound with id 1")
      }

      it("contact event should return a list of errors if ndelius gateway service returns error") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.NDELIUS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )

        whenever(deliusGateway.getContactEventForPerson(crn, 1, mappaCategories)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = contactEventService.getContactEvent(crn, 1, filters)
        result.data?.shouldBe(null)
        result.errors.shouldBe(errors)
      }

      it("contact event should return a contact event from the gateway") {
        val result = contactEventService.getContactEvent(crn, 1, filters)
        result.data?.shouldBe(contactEvents.content.first().toContactEvent())
        result.errors.count().shouldBe(0)
      }

      it("contact event should request all mappa categories when mappa cats are null") {
        whenever(deliusGateway.getContactEventForPerson(any(), any(), any()))
          .thenReturn(Response(contactEvents.content.first()))
        val noMappaFilter = ConsumerFilters()
        contactEventService.getContactEvent(crn, 1, noMappaFilter)
        val captor = argumentCaptor<List<Number>>()
        verify(deliusGateway, times(1)).getContactEventForPerson(any(), any(), captor.capture())
        assertThat(captor.lastValue).hasSize(4)
      }

      it("contact events should request all mappa categories when mappa cats are null") {
        whenever(deliusGateway.getContactEventsForPerson(any(), any(), any(), any()))
          .thenReturn(Response(contactEvents))
        val noMappaFilter = ConsumerFilters()
        contactEventService.getContactEvents(crn, 1, 10, noMappaFilter)
        val captor = argumentCaptor<List<Number>>()
        verify(deliusGateway, times(1)).getContactEventsForPerson(any(), any(), any(), captor.capture())
        assertThat(captor.lastValue).hasSize(4)
      }

      it("contact event should request no mappa categories when mappa cats are empty") {
        whenever(deliusGateway.getContactEventForPerson(any(), any(), any()))
          .thenReturn(Response(contactEvents.content.first()))
        val noMappaFilter = ConsumerFilters(mappaCategories = emptyList())
        contactEventService.getContactEvent(crn, 1, noMappaFilter)
        val captor = argumentCaptor<List<Number>>()
        verify(deliusGateway, times(1)).getContactEventForPerson(any(), any(), captor.capture())
        assertThat(captor.lastValue).hasSize(0)
      }
    },
  )
