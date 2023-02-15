package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ImageMetadata

@Service
class GetImageMetadataForPersonService {
  fun execute(id: String): List<ImageMetadata> {
    return listOf()
  }
}
