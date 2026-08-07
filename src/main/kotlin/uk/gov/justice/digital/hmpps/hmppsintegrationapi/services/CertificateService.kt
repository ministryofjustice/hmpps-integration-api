package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import kotlin.time.Instant

@Component
class CertificateService(
  private val authorisationConfig: AuthorisationConfig,
) {
  fun validateCertificate(
    serialNum: String?,
    expiry: String?,
  ): CertificateSummary = CertificateSummary()

  /**
   * Converts the certificate serial number sent in the header into hex format
   * e.g 9572494320151578633330348943480876283449388176
   * becomes 01:7B:EB:77:06:DB:11:F5:2E:B6:F7:37:7B:A9:E0:E4:84:C5:2C:A3
   */
  fun extractCertificateSerialNumber(serialNumber: String?): String? =
    serialNumber?.let {
      runCatching {
        serialNumber.toBigInteger().toByteArray().toHexString(
          format =
            HexFormat {
              upperCase = true
              bytes.byteSeparator = ":"
            },
        )
      }.getOrNull()
    }
}

class CertificateSummary {
  val isRevoked: Boolean = false
  val expiresAt: Instant? = null
}
