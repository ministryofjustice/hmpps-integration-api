package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetImageService

@RestController
@RequestMapping("/images")
class ImageController(
  @Autowired val getImageService: GetImageService
) {
  @GetMapping("{id}")
  fun getImage(@PathVariable id: String): ByteArray {
    return byteArrayOf()
  }
}
