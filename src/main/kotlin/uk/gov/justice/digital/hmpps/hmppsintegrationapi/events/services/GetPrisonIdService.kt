package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationevents.gateway.PrisonerSearchGateway

@Service
class GetPrisonIdService(
  @Autowired val prisonerSearchGateway: PrisonerSearchGateway,
) {
  fun execute(nomsNumber: String): String? {
    val prisoner = prisonerSearchGateway.getPrisoner(nomsNumber)

    return prisoner?.prisonId
  }
}
