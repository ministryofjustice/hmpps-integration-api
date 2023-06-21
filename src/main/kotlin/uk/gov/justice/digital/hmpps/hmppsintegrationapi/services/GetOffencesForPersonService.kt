package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response

@Service
class GetOffencesForPersonService(
  @Autowired val nomisGateway: NomisGateway,
) {
  fun execute(pncId: String): Response<List<Offence>>? { // TAKE OFF THE NULLABLE ?
    return null
  }
}
