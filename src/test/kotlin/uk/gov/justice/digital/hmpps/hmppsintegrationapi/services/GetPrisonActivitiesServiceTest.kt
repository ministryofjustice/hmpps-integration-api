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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesRunningActivity
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPrisonActivitiesService::class],
)
class GetPrisonActivitiesServiceTest(
  @MockitoBean val activitiesGateway: ActivitiesGateway,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  val getPrisonActivitiesService: GetPrisonActivitiesService,
) : DescribeSpec(
    {
      val prisonId = "MDI"
      val filters = ConsumerFilters(prisons = listOf(prisonId))

      beforeEach {
        Mockito.reset(consumerPrisonAccessService, activitiesGateway)

        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Any>(prisonId, filters, upstreamServiceType = UpstreamApi.ACTIVITIES)).thenReturn(Response(data = null, errors = emptyList()))
      }

      it("should return prison activities") {
        val activities =
          listOf(
            ActivitiesRunningActivity(
              id = 123456L,
              activityName = "Maths level 1",
              category =
                ActivitiesActivityCategory(
                  id = 1,
                  code = "LEISURE_SOCIAL",
                  name = "Leisure and social",
                  description = "Such as association, library time and social clubs, like music or art",
                ),
              capacity = 10,
              allocated = 2,
              waitlisted = 2,
              createdTime = "2020-04-04T10:42:43",
              activityState = "LIVE",
            ),
          )

        whenever(activitiesGateway.getAllRunningActivities(prisonId)).thenReturn(Response(data = activities))

        val result = getPrisonActivitiesService.execute(prisonId, filters)
        result.data.shouldBe(activities.map { it.toRunningActivity() })
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

        val result = getPrisonActivitiesService.execute(prisonId, filters)
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
        whenever(activitiesGateway.getAllRunningActivities(prisonId)).thenReturn(Response(data = null, errors = errors))

        val result = getPrisonActivitiesService.execute(prisonId, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(errors)
      }
    },
  )
