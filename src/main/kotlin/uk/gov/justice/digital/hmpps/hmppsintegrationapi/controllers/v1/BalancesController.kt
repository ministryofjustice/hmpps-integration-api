package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AccountBalance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Balances
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse

@RestController
@RequestMapping("/v1/prison/{prisonId}/prisoners/{hmppsId}/balances")
class BalancesController(
) {
  @GetMapping()
  fun getBalancesForPerson(): DataResponse<Balances> = DataResponse(
    Balances(
      accountBalances = listOf(
        AccountBalance("spends", 101),
        AccountBalance("saving", 102),
        AccountBalance("cash", 103),
      )
    )
  )

}
