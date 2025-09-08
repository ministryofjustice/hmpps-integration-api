package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.ContactEventStubGenerator

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
      val contactEvents = ContactEventStubGenerator.generateNDeliusContactEvents(crn, 10, 1, 100)

      beforeEach {

        Mockito.reset(prisonApiGateway)
        Mockito.reset(personService)
        Mockito.reset(deliusGateway)

        whenever(personService.execute(hmppsId = hmppsId)).thenReturn(Response(person))
        whenever(deliusGateway.getContactEventsForPerson(crn, 1, 10))
          .thenReturn(Response(contactEvents))

        whenever(deliusGateway.getContactEventForPerson(crn, 1))
          .thenReturn(Response(contactEvents.contactEvents.first()))
      }

      it("contact events calls personService search with hmppsId") {
        contactEventService.getContactEvents(hmppsId, 1, 10)
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
            contactEventService.getContactEvents("notfound", 1, 10)
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

        whenever(deliusGateway.getContactEventsForPerson(crn, 1, 10)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = contactEventService.getContactEvents(crn, 1, 10)
        result.data?.content.shouldBe(null)
        result.errors.shouldBe(errors)
      }

      it("contact events should return contact events from gateway") {
        val result = contactEventService.getContactEvents(crn, 1, 10)
        result.data?.content.shouldBe(contactEvents.contactEvents.map { it.toContactEvent() })
        result.errors.count().shouldBe(0)
      }

      it("contact event calls personService search with hmppsId") {
        contactEventService.getContactEvent(hmppsId, 1)
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
            contactEventService.getContactEvent("notfound", 1)
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

        whenever(deliusGateway.getContactEventForPerson(crn, 1)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = contactEventService.getContactEvent(crn, 1)
        result.data?.shouldBe(null)
        result.errors.shouldBe(errors)
      }

      it("contact event should return a contact event from the gateway") {
        val result = contactEventService.getContactEvent(crn, 1)
        result.data?.shouldBe(contactEvents.contactEvents.first().toContactEvent())
        result.errors.count().shouldBe(0)
      }
    },
  )
