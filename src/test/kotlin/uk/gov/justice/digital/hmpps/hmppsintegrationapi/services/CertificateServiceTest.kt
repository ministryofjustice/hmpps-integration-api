package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.matchers.collections.shouldHaveSize
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.ConfigTest
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.assertEquals

class CertificateServiceTest : ConfigTest() {
  val config = AuthorisationConfig()
  val fixedClock: Clock = Clock.fixed(LocalDateTime.of(2026, 5, 8, 12, 30, 10).toInstant(ZoneOffset.UTC), ZoneId.systemDefault())
  val certificateService = CertificateService(config, mockTelemetryService, fixedClock)

  @BeforeEach
  fun setUp() {
    reset(mockTelemetryService)
  }

  @Test
  fun `does not alert for a 1 digit day cert-expiry-date that expires in over 30 days`() {
    val dateString = certificateService.processCertificateExpiryDate("Jun 8 12:30:10 2026 GMT", "consumer-name")
    assertEquals("2026-06-08T12:30:10Z", dateString)
    verify(mockTelemetryService, times(0)).captureMessage(any())
  }

  @Test
  fun `alerts a 2 digit day cert-expiry-date that expires in 30 days`() {
    val dateString = certificateService.processCertificateExpiryDate("Jun 7 12:30:10 2026 GMT", "consumer-name")
    assertEquals("2026-06-07T12:30:10Z", dateString)
    verify(mockTelemetryService, times(1)).captureMessage("The certificate for consumer-name will expire in under 30 days (Jun 7 12:30:10 2026 GMT)")
  }

  @Test
  fun `alerts a cert-expiry-date that expires in 21 days`() {
    val dateString = certificateService.processCertificateExpiryDate("May 29 00:30:10 2026 GMT", "consumer-name")
    assertEquals("2026-05-29T00:30:10Z", dateString)
    verify(mockTelemetryService, times(1)).captureMessage("The certificate for consumer-name will expire in under 3 weeks (May 29 00:30:10 2026 GMT)")
  }

  @Test
  fun `alerts for a cert-expiry-date that expires in 20 days`() {
    val dateString = certificateService.processCertificateExpiryDate("May 28 00:30:10 2026 GMT", "consumer-name")
    assertEquals("2026-05-28T00:30:10Z", dateString)
    verify(mockTelemetryService, times(1)).captureMessage("The certificate for consumer-name will expire in under 3 weeks (May 28 00:30:10 2026 GMT)")
  }

  @Test
  fun `alerts for a cert-expiry-date that expires in 14 days`() {
    val dateString = certificateService.processCertificateExpiryDate("May 22 00:30:10 2026 GMT", "consumer-name")
    assertEquals("2026-05-22T00:30:10Z", dateString)
    verify(mockTelemetryService, times(1)).captureMessage("The certificate for consumer-name will expire in under 2 weeks (May 22 00:30:10 2026 GMT)")
  }

  @Test
  fun `does not alert for a cert-expiry-date that expires in 12 days`() {
    val dateString = certificateService.processCertificateExpiryDate("May 20 00:30:10 2026 GMT", "consumer-name")
    assertEquals("2026-05-20T00:30:10Z", dateString)
    verify(mockTelemetryService, times(1)).captureMessage("The certificate for consumer-name will expire in under 2 weeks (May 20 00:30:10 2026 GMT)")
  }

  @Test
  fun `alerts for a cert-expiry-date that expires in 7 days`() {
    val dateString = certificateService.processCertificateExpiryDate("May 15 00:30:10 2026 GMT", "consumer-name")
    assertEquals("2026-05-15T00:30:10Z", dateString)
    verify(mockTelemetryService, times(1)).captureMessage("The certificate for consumer-name will expire in 7 days (May 15 00:30:10 2026 GMT)")
  }

  @Test
  fun `alerts for a cert-expiry-date header that expires in 1 day`() {
    val dateString = certificateService.processCertificateExpiryDate("May 9 00:30:10 2026 GMT", "consumer-name")
    assertEquals("2026-05-09T00:30:10Z", dateString)
    verify(mockTelemetryService, times(1)).captureMessage("The certificate for consumer-name will expire in 1 day (May 9 00:30:10 2026 GMT)")
  }

  @Test
  fun `alerts for a cert-expiry-date header that expires in 0 days`() {
    val dateString = certificateService.processCertificateExpiryDate("May 8 00:30:10 2026 GMT", "consumer-name")
    assertEquals("2026-05-08T00:30:10Z", dateString)
    verify(mockTelemetryService, times(1)).captureMessage("The certificate for consumer-name will expire in 0 days (May 8 00:30:10 2026 GMT)")
  }

  @Test
  fun `throws an exception if already expired`() {
    val exception =
      assertThrows<RuntimeException> {
        certificateService.processCertificateExpiryDate("May 7 00:30:10 2026 GMT", "consumer-name")
      }
    assertThat(exception.message).isEqualTo("The certificate for consumer-name with expiry date May 7 00:30:10 2026 GMT has expired")
  }

  @Test
  fun `handles an SSL single digit day date with multiple spaces`() {
    val dateString = certificateService.processCertificateExpiryDate("Jan  8 11:23:46 2027 GMT", "consumer-name")
    assertEquals("2027-01-08T11:23:46Z", dateString)
  }

  @Test
  fun `handles an invalid format cert-expiry-date header and logs to sentry`() {
    val dateString = certificateService.processCertificateExpiryDate("Wrong format", "consumer-name")
    assertEquals(null, dateString)
    val exception = argumentCaptor<Throwable>()
    verify(mockTelemetryService, times(1)).captureException(exception.capture())
    assertThat(exception.firstValue.message).contains("Failed to parse certificate expiry date")
  }

  @Test
  fun `does not create a message for days over 30`() {
    val warningMessage = certificateService.expiryWarningMessage(31, "May 7 00:30:10 2026 GMT", "consumer-name")
    assertThat(warningMessage).isEqualTo(null)
  }

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.AuthorisationServiceTest#expiryBandTestArgs")
  fun `creates the same message for each band`(
    days: List<Long>,
    expectedNumberOfDistinctMessages: Int,
  ) {
    val certificateExpiry = "May 7 00:30:10 2026 GMT"
    val messages = days.map { days -> certificateService.expiryWarningMessage(days, certificateExpiry, "consumer-name") }
    messages.toSet().shouldHaveSize(expectedNumberOfDistinctMessages)
  }

  @Test
  fun `throws an exception when days are negative`() {
    val exception =
      assertThrows<RuntimeException> {
        certificateService.expiryWarningMessage(-1, "May 7 00:30:10 2026 GMT", "consumer-name")
      }
    assertThat(exception.message).isEqualTo("The certificate for consumer-name with expiry date May 7 00:30:10 2026 GMT has expired")
  }
}
