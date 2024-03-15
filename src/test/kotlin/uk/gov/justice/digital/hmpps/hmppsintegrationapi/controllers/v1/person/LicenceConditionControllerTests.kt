package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Licence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LicenceCondition
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonLicences
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetLicenceConditionService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@WebMvcTest(controllers = [LicenceConditionController::class])
@ActiveProfiles("test")
class LicenceConditionControllerTests(
  @Autowired var springMockMvc: MockMvc,
  @MockBean val getLicenceConditionService: GetLicenceConditionService,
  @MockBean val auditService: AuditService,
) : DescribeSpec(
  {
    val hmppsId = "9999/11111A"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val path = "/v1/persons/$encodedHmppsId/licences/conditions"
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)

    describe("GET $path") {
      beforeTest {
        Mockito.reset(getLicenceConditionService)
        Mockito.reset(auditService)
        whenever(getLicenceConditionService.execute(hmppsId)).thenReturn(
          Response(
            data = PersonLicences(
              hmppsId = hmppsId,
              licences = listOf(
                Licence(
                  id = "MockId",
                  conditions = listOf(LicenceCondition(condition = "MockCondition")),
                ),
              ),
            ),
          ),
        )
      }

      it("throws exception when no person found") {
        whenever(getLicenceConditionService.execute(hmppsId = "notfound")).thenReturn(
          Response<PersonLicences>(
            data = PersonLicences(hmppsId = hmppsId, licences = emptyList()),
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

      it("returns licence condition results") {

        val result = mockMvc.performAuthorised(path)
        result.response.contentAsString.shouldContain(
          """
           "data":{
                "hmppsId":"9999/11111A",
                "offenderNumber":null,
                "licences":[
                   {
                      "status":null,
                      "typeCode":null,
                      "createdDate":null,
                      "approvedDate":null,
                      "updatedDate":null,
                      "conditions":[
                         {
                            "type":null,
                            "code":null,
                            "category":null,
                            "condition":"MockCondition"
                         }
                      ]
                   }
                ]
        """.removeWhitespaceAndNewlines(),
        )
      }

      it("fails with the appropriate error when an upstream service is down") {
        whenever(getLicenceConditionService.execute(hmppsId)).doThrow(
          WebClientResponseException(500, "MockError", null, null, null, null),
        )

        val response = mockMvc.performAuthorised(path)

        assert(response.response.status == 500)
        assert(response.response.contentAsString.equals("{\"status\":500,\"errorCode\":null,\"userMessage\":\"500 MockError\",\"developerMessage\":\"Unable to complete request as an upstream service is not responding\",\"moreInfo\":null}"))
      }
    }
  },
)
