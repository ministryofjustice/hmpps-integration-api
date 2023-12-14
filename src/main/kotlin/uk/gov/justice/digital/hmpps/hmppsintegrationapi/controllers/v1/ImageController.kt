package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetImageService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/images")
class ImageController(
  @Autowired val getImageService: GetImageService,
  @Autowired val auditService: AuditService,
) {
  @GetMapping("{id}")
  fun getImage(@PathVariable id: Int): ResponseEntity<ByteArray> {
    val response = getImageService.execute(id)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find image with id: $id")
    }

    auditService.createEvent("GET_PERSON_IMAGE", "Image with id: $id has been retrieved")

    return ResponseEntity.ok()
      .header("content-type", "image/jpeg")
      .body(response.data)
  }
}
