package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.fixedClock
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.ceil

@Component
class CertificateService(
  private val authorisationConfig: AuthorisationConfig,
  private val telemetryService: TelemetryService,
  private val clock: Clock = fixedClock(),
) {
  fun validateCertificate(
    commonName: String,
    rawSerialNum: String?,
    rawExpiryTime: String?,
  ): CertificateInfo {
    val serialNumber = toHexFormat(rawSerialNum)
    return CertificateInfo(
      serialNumber,
      commonName,
      certificateRevoked(serialNumber, commonName),
      processCertificateExpiryDate(rawExpiryTime, commonName),
    )
  }

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
    certificateSerialNumber: String? = null,
    consumerName: String,
  ): Boolean {
    if (certificateSerialNumber == null) {
      return false
    }
    certificateRevocationList().forEach {
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
  fun toHexFormat(serialNumber: String?): String? =
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

  /**
   * Converts a certificate expiry date in the OpenSSL format to an ISO-6801 format
   * Creates a sentry alert if the number of days to expiry is in the range 30, 21, 14, 7..0
   * If the date is not in the OpenSSL format, will capture exception and return null
   * Throws a RuntimeException if the certificate has already expired
   *
   * @param certExpiryDate The certificate expiry date in the OpenSSL format e.g Jun 7 12:30:10 2026 GMT
   * @param consumerName The consumer name
   * @return The certificate expiry date in ISO-8601 format
   */

  fun processCertificateExpiryDate(
    certExpiryDate: String?,
    consumerName: String,
  ): Instant? {
    if (certExpiryDate == null) {
      return null
    }
    val expiryDateTime =
      try {
        // OpenSSL notAfter date still uses 2 characters for a single digit day (with the first blank). eg Jan  8 12:30:10 2026 GMT
        // Therefore we strip any double whitespaces
        ZonedDateTime
          .parse(certExpiryDate.replace("\\s{2,}".toRegex(), " "), DateTimeFormatter.ofPattern("MMM d HH:mm:ss yyyy zzz", Locale.ENGLISH))
          .toInstant()
      } catch (ex: Exception) {
        telemetryService.captureException(RuntimeException("Failed to parse certificate expiry date $certExpiryDate. ${ex.message}"))
        null
      }
    return expiryDateTime?.let {
      checkExpiryDate(expiryDateTime, certExpiryDate, consumerName)
      expiryDateTime
    }
  }

  fun checkExpiryDate(
    expiryDateTime: java.time.Instant,
    certExpiryDateString: String,
    consumerName: String,
  ) {
    val today = LocalDate.ofInstant(clock.instant(), clock.zone)
    val expires = LocalDate.ofInstant(expiryDateTime, clock.zone)
    val days = ChronoUnit.DAYS.between(today, expires)

    val expiryWarningMessage = expiryWarningMessage(days, certExpiryDateString, consumerName)

    if (expiryWarningMessage != null) {
      telemetryService.captureMessage(expiryWarningMessage)
    }
  }

  fun expiryWarningMessage(
    days: Long,
    expiryDateTime: String,
    consumerName: String,
  ): String? {
    val durationMessage =
      when {
        (days < 0) -> throw RuntimeException("The certificate for $consumerName with expiry date $expiryDateTime has expired")
        (days <= 7) -> "in $days ${if (days == 1L) "day" else "days"}"
        (days <= 28) -> {
          val weeks = ceil(days / 7.0).toInt()
          "in under $weeks ${if (weeks == 1) "week" else "weeks"}"
        }
        (days <= 30) -> "in under 30 days"
        else -> null
      }
    return durationMessage?.let {
      "The certificate for $consumerName will expire $durationMessage ($expiryDateTime)"
    }
  }

  fun certificateRevocationList() = authorisationConfig.certificateRevocationList

  fun extractConsumerName(subjectDistinguishedName: String): String? {
    val match = Regex("^.*,CN=(.*)$").find(subjectDistinguishedName)

    if (match?.groupValues == null) {
      return null
    }
    return match.groupValues[1]
  }
}

data class CertificateInfo(
  val seriaNumber: String? = null,
  val commonName: String? = null,
  val isRevoked: Boolean = false,
  val expiresAt: Instant? = null,
)
