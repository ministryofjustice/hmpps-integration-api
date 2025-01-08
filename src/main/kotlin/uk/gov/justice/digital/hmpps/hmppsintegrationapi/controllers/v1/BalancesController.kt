package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AccountBalance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Balances
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetBalancesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetOffencesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse

@RestController
@RequestMapping("/v1/prison/{prisonId}/prisoners/{hmppsId}/balances")
class BalancesController(@Autowired val getBalancesForPersonService: GetBalancesForPersonService,) {
  @GetMapping()
  fun getBalancesForPerson(@PathVariable hmppsId: String, @PathVariable prisonId: String): Response<Balances?> {
    val response = getBalancesForPersonService.execute(prisonId, hmppsId)

    return response
  }
}
