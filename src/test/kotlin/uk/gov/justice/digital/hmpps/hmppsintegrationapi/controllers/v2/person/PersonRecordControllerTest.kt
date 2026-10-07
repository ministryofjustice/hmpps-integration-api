package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v2.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import jakarta.validation.ValidationException
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v2.PersonRecordController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CPRName
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CorePersonRecord
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.v2.PersonRecordService

@WebMvcTest(controllers = [PersonRecordController::class])
@Import(WebMvcTestConfiguration::class)
@ActiveProfiles("test")
internal class PersonRecordControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val personRecordService: PersonRecordService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec(
    {
      val hmppsId = "A123456"
      val path = "/v2/persons/$hmppsId"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      describe("GET $path") {
        beforeTest {
          Mockito.reset(personRecordService)
          whenever(personRecordService.personRecord(eq(hmppsId), any<RequestContext>())).thenReturn(
            Response(CorePersonRecord(CPRName("John", "Brian", "Doe"))),
          )
          Mockito.reset(auditService)
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          val response = result.response.contentAsJson<DataResponse<CorePersonRecord>>()
          response.data.name
            ?.firstName
            .shouldBe("John")

          verify(auditService, times(1)).createEvent(
            "GET_PERSON_RECORD",
            mapOf("hmppsId" to hmppsId),
          )
        }

        it("returns a bad request from upstream") {
          whenever(personRecordService.personRecord(eq(hmppsId), any<RequestContext>())).thenThrow(ValidationException("Bad Request"))
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }
      }
    },
  )
