package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person

@Component
class NomisGateway {
  fun getPerson(id: Int): Person? {
    return null
  }
}
