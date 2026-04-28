package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig

/**
 * Checks whether the certificate serial number exists in the certificate revocation list in application.yaml
 * If the entry contains a "/" then the entry only applies to the consumer name that follows the "/"
 * e.g for these 2 entries in application.yaml
 * authorisation:
 *  certificate-revocation-list:
 *    - 01:7b:eb:77:06:db:11:f5:2e:b6:f7:37:7b:a9:e0:e4:84:c5:2c:a3
 *    - 01/a-consumer
 *
 * The first entry would apply globally. The second entry would only apply to a consumer with name a-consumer
 */
fun certificateRevoked(
  authorisationConfig: AuthorisationConfig,
  certificateSerialNumber: String,
  consumerName: String,
): Boolean {
  authorisationConfig.certificateRevocationList.forEach {
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
