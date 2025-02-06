package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Balances
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.PerformanceTestService

@RestController
@RequestMapping("/v1/performance-test")
@Hidden
class PerformanceTestController(
  @Autowired val performanceTestService: PerformanceTestService,
) {
  @GetMapping("/test-1/{prisonId}/{hmppsId}")
  fun getBalancesForPerson(
    @RequestAttribute filters: ConsumerFilters?,
    @PathVariable hmppsId: String,
    @PathVariable prisonId: String,
  ): DataResponse<Balances?> {
    val response = performanceTestService.execute(prisonId, hmppsId, filters = filters)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Not found")
    }

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException()
    }

    return DataResponse(response.data)
  }
}
