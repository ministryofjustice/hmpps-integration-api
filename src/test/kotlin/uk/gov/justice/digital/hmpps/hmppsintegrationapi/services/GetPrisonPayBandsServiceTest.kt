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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesPrisonPayBand
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPrisonPayBandsService::class],
)
class GetPrisonPayBandsServiceTest(
  @MockitoBean val activitiesGateway: ActivitiesGateway,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  val getPrisonPayBandsService: GetPrisonPayBandsService,
) : DescribeSpec(
    {
      val prisonId = "MDI"
      val filters = ConsumerFilters(prisons = listOf(prisonId))

      beforeEach {
        Mockito.reset(consumerPrisonAccessService, activitiesGateway)

        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Any>(prisonId, filters, upstreamServiceType = UpstreamApi.ACTIVITIES)).thenReturn(Response(data = null, errors = emptyList()))
      }

      it("should return prison pay bands") {
        val activitiesPrisonPayBand =
          listOf(
            ActivitiesPrisonPayBand(
              id = 123456,
              displaySequence = 1,
              alias = "Low",
              description = "Pay band 1",
              nomisPayBand = 1,
              prisonCode = "MDI",
              createdTime = "2025-06-13T14:25:19.385Z",
              createdBy = "string",
              updatedTime = "2025-06-13T14:25:19.385Z",
              updatedBy = "string",
            ),
          )

        whenever(activitiesGateway.getPrisonPayBands(prisonId)).thenReturn(Response(data = activitiesPrisonPayBand))

        val result = getPrisonPayBandsService.execute(prisonId, filters)
        result.data.shouldBe(activitiesPrisonPayBand.map { it.toPrisonPayBand() })
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

        val result = getPrisonPayBandsService.execute(prisonId, filters)
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
        whenever(activitiesGateway.getPrisonPayBands(prisonId)).thenReturn(Response(data = null, errors = errors))

        val result = getPrisonPayBandsService.execute(prisonId, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(errors)
      }
    },
  )
