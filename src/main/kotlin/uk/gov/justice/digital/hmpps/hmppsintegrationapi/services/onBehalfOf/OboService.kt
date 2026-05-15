package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onBehalfOf

import io.jsonwebtoken.Claims
import org.springframework.stereotype.Component

@Component
interface OboService {
  fun decodeJwt(jwtToken: String): Claims?
}
