package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Licence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LicenceCondition
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetLicenseConditionService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@WebMvcTest(controllers = [LicenceConditionController::class])
@ActiveProfiles("test")
class LicenceConditionControllerTests(
  @Autowired var springMockMvc: MockMvc,
  @MockBean val getLicenseConditionService: GetLicenseConditionService,
  @MockBean val auditService: AuditService,
) : DescribeSpec(
  {
    val hmppsId = "9999/11111A"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val path = "/v1/persons/$encodedHmppsId/licences/conditions"
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)

    describe("GET $path") {
      beforeTest {
        Mockito.reset(getLicenseConditionService)
        Mockito.reset(auditService)
        whenever(getLicenseConditionService.execute(hmppsId)).thenReturn(
          Response(
            data = listOf(
              Licence(
                id = "MockId",
                conditions = listOf(LicenceCondition(condition = "MockCondition")),
              ),
            ),
          ),
        )
      }

      it("throws exception when no person found") {
        whenever(getLicenseConditionService.execute(hmppsId = "notfound")).thenReturn(
          Response(
            data = emptyList(),
            errors = listOf(
              UpstreamApiError(
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                causedBy = UpstreamApi.CVL,
              ),
            ),
          ),
        )
        val noFoundPath = "/v1/persons/notfound/licences/conditions"
        val result = mockMvc.performAuthorised(noFoundPath)
        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }

      it("logs audit for licence condition") {
        mockMvc.performAuthorised(path)

        verify(auditService, VerificationModeFactory.times(1)).createEvent("GET_PERSON_LICENCE_CONDITION", "Person licence condition details with hmpps id: $hmppsId has been retrieved")
      }

      it("returns paginated licence condition results") {
        whenever(getLicenseConditionService.execute(hmppsId)).thenReturn(
          Response(
            data = List(20) {
              Licence(
                id = "MockId",
                conditions = listOf(LicenceCondition(condition = "MockCondition")),
              )
            },
          ),
        )
        val result = mockMvc.performAuthorised("$path?page=1&perPage=15")

        result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.page", 1)
        result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.totalPages", 2)
      }
    }
  },
)
