package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway

@Service
class GetImageService(@Autowired val nomisGateway: NomisGateway) {
  fun execute(id: Int): ByteArray = nomisGateway.getImageData(id)
}
