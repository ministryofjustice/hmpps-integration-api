package uk.gov.justice.digital.hmpps.hmppsintegrationapi.common

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext

interface CrnSupplier {
  fun getCrn(
    hmppsId: String,
    requestContext: RequestContext? = null,
  ): String?

  companion object {
    val CRN_REGEX = "^[A-Z]\\d{6}$".toRegex()
  }
}
