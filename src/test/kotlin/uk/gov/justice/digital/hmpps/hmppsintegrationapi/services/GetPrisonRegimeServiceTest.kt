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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesPrisonRegime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters
import java.time.DayOfWeek

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPrisonRegimeService::class],
)
class GetPrisonRegimeServiceTest(
  @MockitoBean val activitiesGateway: ActivitiesGateway,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  val getPrisonRegimeService: GetPrisonRegimeService,
) : DescribeSpec(
    {
      val prisonId = "MDI"
      val filters = RoleFilters(prisons = listOf(prisonId))

      beforeEach {
        Mockito.reset(consumerPrisonAccessService, activitiesGateway)

        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Any>(prisonId, filters, upstreamServiceType = UpstreamApi.ACTIVITIES)).thenReturn(Response(data = null, errors = emptyList()))
      }

      it("should return a prison regime") {
        val activitiesPrisonRegime =
          listOf(
            ActivitiesPrisonRegime(
              id = 123456L,
              prisonCode = prisonId,
              amStart = "09:00",
              amFinish = "12:00",
              pmStart = "13:00",
              pmFinish = "17:00",
              edStart = "18:00",
              edFinish = "21:00",
              dayOfWeek = DayOfWeek.MONDAY,
            ),
          )

        whenever(activitiesGateway.getPrisonRegime(prisonId)).thenReturn(Response(data = activitiesPrisonRegime))

        val result = getPrisonRegimeService.execute(prisonId, filters)
        result.data.shouldBe(activitiesPrisonRegime.map { it.toPrisonRegime() })
        result.errors.shouldBeEmpty()
      }

      it("should return an error if consumer filter check fails") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.ACTIVITIES,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              description = "Error from consumer prison access check",
            ),
          )
        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Any>(prisonId, filters, upstreamServiceType = UpstreamApi.ACTIVITIES)).thenReturn(Response(data = null, errors = errors))

        val result = getPrisonRegimeService.execute(prisonId, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(errors)
      }

      it("should return an error if gateway returns an error") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.ACTIVITIES,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              description = "Error from gateway",
            ),
          )
        whenever(activitiesGateway.getPrisonRegime(prisonId)).thenReturn(Response(data = null, errors = errors))

        val result = getPrisonRegimeService.execute(prisonId, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(errors)
      }
    },
  )
