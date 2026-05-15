package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof

interface OboService {
  fun extractUsername(token: String): String?
}
