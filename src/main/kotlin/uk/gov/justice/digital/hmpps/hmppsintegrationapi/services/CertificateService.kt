package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.fixedClock
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import java.time.Clock
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.ceil
import kotlin.time.Instant

@Component
class CertificateService(
  private val authorisationConfig: AuthorisationConfig,
  private val telemetryService: TelemetryService,
  private val clock: Clock = fixedClock(),
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
    certExpiryDate: String,
    consumerName: String,
  ): String? {
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
      expiryDateTime.toString()
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
}

data class CertificateSummary(
  val isRevoked: Boolean = false,
  val expiresAt: Instant? = null,
)
