package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway

@Service
class GetPrisonIdService(
  @Autowired val prisonerSearchGateway: PrisonerOffenderSearchGateway,
) {
  fun execute(nomsNumber: String): String? {
    val prisoner = prisonerSearchGateway.getPrisonOffender(nomsNumber)

    return prisoner.data?.prisonId
  }
}
