package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.boot.context.properties.bind.Name
import org.springframework.context.annotation.Configuration

/**
 * Basic implementation of a Certificate Revocation List.
 *
 * The list of revoked certificates is in application.yaml
 */
@Configuration
data class CertificateRevocationList(
  @Name("authorisation.certificate-revocation-list")
  val revokedCertificates: List<String> = emptyList(),
) {
  /**
   * Returns true if the specified certificate serial number is in the revocation list.
   *
   * Entries in the CRL can be tied to specific consumer names because the early certificates
   * were all issued with serial number "1".
   */
  fun isRevoked(
    certificateSerialNumber: String,
    consumerName: String,
  ): Boolean {
    revokedCertificates.forEach {
      val entry = it.split("/")
      val serialNumber = entry[0]
      val thisConsumerOnly = if (entry.size > 1) entry[1] else null
      if (thisConsumerOnly != null) {
        if (serialNumber.equals(certificateSerialNumber, ignoreCase = true) && thisConsumerOnly == consumerName) {
          return true
        }
      } else {
        if (serialNumber.equals(certificateSerialNumber, ignoreCase = true)) {
          return true
        }
      }
    }
    return false
  }

  /**
   * Converts the certificate serial number sent in the header into hex format
   * e.g 9572494320151578633330348943480876283449388176
   * becomes 01:7B:EB:77:06:DB:11:F5:2E:B6:F7:37:7B:A9:E0:E4:84:C5:2C:A3
   */
  fun normaliseCertificateSerialNumber(serialNumber: String?): String? =
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
