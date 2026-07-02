package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.WebMvcTestConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.MovementDatesEntry
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiPrisonTimeline
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonPeriodEntry
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.TransfersEntry
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonTimelineForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [PrisonTimelineController::class])
@Import(WebMvcTestConfiguration::class)
@ActiveProfiles("test")
internal class PrisonTimelineControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getPrisonTimelineForPersonService: GetPrisonTimelineForPersonService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec(
    {
      val hmppsId = "A123456"
      val path = "/v1/persons/$hmppsId/prison-timeline"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)
      val filters = null
      val requestContext = buildRequestContext("testUser", filters = filters)

      describe("GET $path") {
        beforeTest {
          Mockito.reset(getPrisonTimelineForPersonService)
          whenever(getPrisonTimelineForPersonService.getPrisonTimeline(any(), any())).thenReturn(
            Response(
              PrisonApiPrisonTimeline(
                prisonerNumber = "A7748DZ",
                prisonPeriod =
                  listOf(
                    PrisonPeriodEntry(
                      bookNumber = "47828A",
                      bookingId = 1211013,
                      entryDate = "2023-12-08T15:50:37",
                      releaseDate = "2023-12-08T16:21:24",
                      movementDates =
                        listOf(
                          MovementDatesEntry(
                            reasonInToPrison = "Imprisonment Without Option",
                            dateInToPrison = "2023-12-08T15:50:37",
                            inwardType = "ADM",
                            reasonOutOfPrison = "Wedding/Civil Ceremony",
                            dateOutOfPrison = "2023-12-08T15:53:37",
                            outwardType = "TAP",
                            admittedIntoPrisonId = "BMI",
                            releaseFromPrisonId = "BSI",
                          ),
                        ),
                      transfers =
                        listOf(
                          TransfersEntry(
                            dateOutOfPrison = "2023-12-08T15:51:09",
                            dateInToPrison = "2023-12-08T15:52:32",
                            transferReason = "Compassionate Transfer",
                            fromPrisonId = "BMI",
                            toPrisonId = "BSI",
                          ),
                        ),
                      prisons = listOf("BMI", "BSI"),
                    ),
                  ),
              ),
            ),
          )
          Mockito.reset(auditService)
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("gets prison timeline for a person with the matching ID") {
          mockMvc.performAuthorised(path)
          verify(getPrisonTimelineForPersonService, VerificationModeFactory.times(1)).getPrisonTimeline(eq(hmppsId), any())
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)

          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("GET_PERSON_PRISON_TIMELINE", mapOf("hmppsId" to hmppsId))
        }

        it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
          whenever(getPrisonTimelineForPersonService.getPrisonTimeline(eq(hmppsId), any())).thenReturn(
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
          whenever(getPrisonTimelineForPersonService.getPrisonTimeline(eq(hmppsId), any())).thenReturn(
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
    },
  )
