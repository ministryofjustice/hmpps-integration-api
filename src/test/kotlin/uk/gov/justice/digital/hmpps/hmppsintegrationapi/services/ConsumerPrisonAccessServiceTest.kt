package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ConsumerPrisonAccessService::class],
)
internal class ConsumerPrisonAccessServiceTest(
  private val consumerPrisonAccessService: ConsumerPrisonAccessService,
) : DescribeSpec(
    {

      it("Should return no errors when prison id is in filters") {
        val result =
          consumerPrisonAccessService.checkConsumerHasPrisonAccess<Nothing>("MDI", ConsumerFilters(listOf("MDI")))
        result.errors.shouldBeEmpty()
      }

      it("Should return an error when prison id is not in filters") {
        val result =
          consumerPrisonAccessService.checkConsumerHasPrisonAccess<Nothing>("XYZ", ConsumerFilters(listOf("MDI")))
        result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
      }

      it("Should return no errors when prison id is supplied but filters are not") {
        val result =
          consumerPrisonAccessService.checkConsumerHasPrisonAccess<Nothing>("MDI", ConsumerFilters(null))
        result.errors.shouldBeEmpty()
      }

      it("Should return an error when prison id is null but filters is supplied") {
        val result =
          consumerPrisonAccessService.checkConsumerHasPrisonAccess<Nothing>(null, ConsumerFilters(listOf("MDI")))
        result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
      }

      it("Should return no errors when prison id nor the filters are supplied") {
        val result =
          consumerPrisonAccessService.checkConsumerHasPrisonAccess<Nothing>(null, ConsumerFilters(null))
        result.errors.shouldBeEmpty()
      }
    },
  )
