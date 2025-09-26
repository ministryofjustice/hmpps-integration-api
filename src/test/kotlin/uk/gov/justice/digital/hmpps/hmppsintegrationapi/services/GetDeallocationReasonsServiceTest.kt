package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesDeallocationReason
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetDeallocationReasonsService::class],
)
class GetDeallocationReasonsServiceTest(
  @MockitoBean val activitiesGateway: ActivitiesGateway,
  val getDeallocationReasonsService: GetDeallocationReasonsService,
) : DescribeSpec(
    {
      val activitiesDeallocationReason =
        ActivitiesDeallocationReason(
          code = "RELEASED",
          description = "Released from prison",
        )
      beforeEach {
        Mockito.reset(activitiesGateway)
      }

      it("should return deallocation reasons") {
        whenever(activitiesGateway.getDeallocationReasons()).thenReturn(Response(data = listOf(activitiesDeallocationReason)))

        val result = getDeallocationReasonsService.execute()
        result.data.shouldBe(listOf(activitiesDeallocationReason.toDeallocationReason()))
        result.errors.shouldBeEmpty()
      }

      it("should return an error if gateway returns an error") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.ACTIVITIES,
              type = UpstreamApiError.Type.FORBIDDEN,
              description = "Error from gateway",
            ),
          )
        whenever(activitiesGateway.getDeallocationReasons()).thenReturn(Response(data = null, errors = errors))

        val result = getDeallocationReasonsService.execute()
        result.data.shouldBeNull()
        result.errors.shouldBe(errors)
      }
    },
  )
