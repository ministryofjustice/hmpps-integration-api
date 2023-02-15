package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ImageMetadata

@Service
class GetImageMetadataForPersonService(@Autowired val nomisGateway: NomisGateway) {
  fun execute(id: String): List<ImageMetadata> = nomisGateway.getImageMetadataForPerson(id)
}
