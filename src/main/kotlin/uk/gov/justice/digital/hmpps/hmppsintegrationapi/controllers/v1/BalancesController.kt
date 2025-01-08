package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.InternalServerErrorException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Balances
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetBalancesForPersonService

@RestController
@RequestMapping("/v1/prison/{prisonId}/prisoners/{hmppsId}/balances")
class BalancesController(@Autowired val getBalancesForPersonService: GetBalancesForPersonService,) {
  @GetMapping()
  fun getBalancesForPerson(@PathVariable hmppsId: String, @PathVariable prisonId: String): Response<Balances?> {
    val response = getBalancesForPersonService.execute(prisonId, hmppsId)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw BadRequestException("Could not find account for person with id: $hmppsId at prison: $prisonId")
    }

    if (response.hasError(UpstreamApiError.Type.INTERNAL_SERVER_ERROR)) {
      throw InternalServerErrorException("Could not find accounts for person with id: $hmppsId")
    }
    return response
  }
}
