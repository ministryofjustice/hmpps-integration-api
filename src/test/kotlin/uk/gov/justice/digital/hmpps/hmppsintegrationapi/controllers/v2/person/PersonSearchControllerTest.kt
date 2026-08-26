package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v2.person

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v2.PersonSearchController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase.Companion.gatewaysFolder
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CPRName
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CorePersonRecordSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CorePersonRecordSearchResponseItem
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.v2.PersonSearchService
import java.io.File

@WebMvcTest(controllers = [PersonSearchController::class])
@Import(WebMvcTestConfiguration::class)
@ActiveProfiles("test")
internal class PersonSearchControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val personSearchService: PersonSearchService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec(
    {
      val path = "/v2/persons"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)
      val request =
        jacksonObjectMapper().readValue(
          File(
            "$gatewaysFolder/cpr/fixtures/core-person-record-search-request.json",
          ).readText(),
          CorePersonRecordSearchRequest::class.java,
        )

      describe("POST $path") {
        beforeTest {
          Mockito.reset(personSearchService)
          whenever(personSearchService.personSearch(eq(request), any<RequestContext>())).thenReturn(
            Response(
              listOf(CorePersonRecordSearchResponseItem(CPRName("John", "Brian", "Doe"))),
            ),
          )
          Mockito.reset(auditService)
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorisedPost(path, request)
          result.response.status.shouldBe(HttpStatus.OK.value())
          val response = result.response.contentAsJson<DataResponse<List<CorePersonRecordSearchResponseItem>>>()
          response.data[0]
            .name
            ?.firstName
            .shouldBe("John")

          verify(auditService, times(1)).createEvent(
            "PERSON_SEARCH_V2",
            request.toAuditableMap(),
          )
        }

        it("returns a bad request from upstream") {
          whenever(personSearchService.personSearch(eq(request), any<RequestContext>())).thenReturn(
            Response(
              null,
              listOf(UpstreamApiError(UpstreamApi.CORE_PERSON_RECORD, UpstreamApiError.Type.BAD_REQUEST)),
            ),
          )
          val result = mockMvc.performAuthorisedPost(path, request)
          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }
      }
    },
  )
