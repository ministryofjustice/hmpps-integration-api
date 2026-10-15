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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.ContactEventHelper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.tier.TierV3DetailsDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.TierService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [TierController::class])
@Import(WebMvcTestConfiguration::class)
@ActiveProfiles("test")
internal class TierControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val tierService: TierService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec(
    {
      val hmppsId = "A123456"
      val path = "/v1/persons/$hmppsId/tier/details"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)
      val events = ContactEventHelper.generateNDeliusContactEvents(hmppsId, 10, 1, 100).content.map { it.toContactEvent() }

      describe("GET $path") {
        beforeTest {
          Mockito.reset(tierService)
          whenever(tierService.getTierDetails(eq(hmppsId), any<RequestContext>())).thenReturn(
            Response(TierV3DetailsDto(calculationId = "1")),
          )
          Mockito.reset(auditService)
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)

          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("GET_TIER_DETAILS", mapOf("hmppsId" to hmppsId))
        }

        it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
          whenever(tierService.getTierDetails(eq(hmppsId), any<RequestContext>())).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.TIER,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }
      }
    },
  )
