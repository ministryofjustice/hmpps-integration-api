package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.ContactEventHelper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactEvents
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ContactEventService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [ContactEventsController::class])
@ActiveProfiles("test")
internal class ContactEventsControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val contactEventService: ContactEventService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec(
    {
      val hmppsId = "A123456"
      val path = "/v1/persons/$hmppsId/contact-events"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)
      val events = ContactEventHelper.generateNDeliusContactEvents(hmppsId, 10, 1, 100).content.map { it.toContactEvent() }
      val filters = null

      describe("GET $path") {
        beforeTest {
          Mockito.reset(contactEventService)

          whenever(contactEventService.getContactEvents(hmppsId, 1, 10, filters)).thenReturn(
            Response(
              ContactEvents(
                content = events,
                isLastPage = false,
                count = 10,
                page = 1,
                perPage = 10,
                totalCount = 100,
                totalPages = 10,
              ),
            ),
          )
          Mockito.reset(auditService)
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("gets the contact events for a person with the matching ID") {
          mockMvc.performAuthorised(path)
          verify(contactEventService, VerificationModeFactory.times(1)).getContactEvents(hmppsId, 1, 10, filters)
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)

          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("GET_PERSON_CONTACT_EVENTS", mapOf("hmppsId" to hmppsId))
        }

        it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
          whenever(contactEventService.getContactEvents(hmppsId, 1, 10, filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PRISON_API,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("returns a 400 BAD REQUEST status code when a bad request is sent to the upstream API") {
          whenever(contactEventService.getContactEvents(hmppsId, 1, 10, filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NDELIUS,
                    type = UpstreamApiError.Type.BAD_REQUEST,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }
      }

      describe("GET $path/2") {
        beforeTest {
          Mockito.reset(contactEventService)
          whenever(contactEventService.getContactEvent(hmppsId, 2, filters)).thenReturn(
            Response(events.first()),
          )
          Mockito.reset(auditService)
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised("$path/2")
          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("gets the contact events for a person with the matching ID") {
          mockMvc.performAuthorised("$path/2")
          verify(contactEventService, VerificationModeFactory.times(1)).getContactEvent(hmppsId, 2, filters)
        }

        it("logs audit") {
          mockMvc.performAuthorised("$path/2")

          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("GET_PERSON_CONTACT_EVENT", mapOf("hmppsId" to hmppsId))
        }

        it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
          whenever(contactEventService.getContactEvent(hmppsId, 2, filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NDELIUS,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised("$path/2")

          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("returns a 400 BAD REQUEST status code when a bad request is sent to the upstream API") {
          whenever(contactEventService.getContactEvent(hmppsId, 2, filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NDELIUS,
                    type = UpstreamApiError.Type.BAD_REQUEST,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised("$path/2")

          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }
      }
    },
  )
