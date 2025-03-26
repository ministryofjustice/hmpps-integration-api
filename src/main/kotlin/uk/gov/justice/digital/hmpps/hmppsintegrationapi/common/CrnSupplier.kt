package uk.gov.justice.digital.hmpps.hmppsintegrationapi.common

interface CrnSupplier {
  fun getCrn(hmppsId: String): String?

  companion object {
    val CRN_REGEX = "^[A-Z]\\d{6}$".toRegex()
  }
}
