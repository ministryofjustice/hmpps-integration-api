package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CourtRegisterGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Court
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetCourtService::class],
)
class GetCourtServiceTest(
  @MockitoBean val courtRegisterGateway: CourtRegisterGateway,
  private val getCourtService: GetCourtService,
) : DescribeSpec(
    {
      val courtId = "ACCRYC"
      val requestContext = buildRequestContext("testUser")

      beforeEach {
        Mockito.reset(courtRegisterGateway)

        whenever(getCourtService.getCourt(courtId, requestContext)).thenReturn(Response(Court("ACCRYC")))
      }

      it("should return court from gateway") {
        val result = getCourtService.getCourt(courtId, requestContext)
        result.data.shouldNotBeNull()
        result.data.courtId
          .shouldNotBeNull()
          .shouldBe("ACCRYC")
        result.errors.count().shouldBe(0)
      }

      it("return errors if court register gateway returns an error") {
        val errors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
              causedBy = UpstreamApi.COURT_REGISTER,
              description = "Mock error from court register gateway",
            ),
          )
        whenever(courtRegisterGateway.getCourt(courtId, requestContext)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = getCourtService.getCourt(courtId, requestContext)
        result.errors.shouldBe(errors)
      }
    },
  )
