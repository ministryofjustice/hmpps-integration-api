package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.CertificateRevocationList
import java.io.IOException

@Component
@Order(0)
@Profile("!local")
class ConsumerNameExtractionFilter(
  @Autowired val certificateRevocationList: CertificateRevocationList,
): Filter {
  @Throws(IOException::class, ServletException::class)
  override fun doFilter(
    request: ServletRequest,
    response: ServletResponse?,
    chain: FilterChain,
  ) {
    val req = request as HttpServletRequest
    response as HttpServletResponse
    val subjectDistinguishedName = req.getHeader("subject-distinguished-name")
    val certificateSerialNumber = certificateRevocationList.normaliseCertificateSerialNumber(req.getHeader("cert-serial-number"))
    val extractedConsumerName = extractConsumerName(subjectDistinguishedName)
    req.setAttribute("clientName", extractedConsumerName)
    req.setAttribute("certificateSerialNumber", certificateSerialNumber)
    chain.doFilter(request, response)
  }

  fun extractConsumerName(subjectDistinguishedName: String?): String? {
    if (subjectDistinguishedName.isNullOrEmpty()) {
      return null
    }

    val match = Regex("^.*,CN=(.*)$").find(subjectDistinguishedName)

    if (match?.groupValues == null) {
      return null
    }

    return match.groupValues[1]
  }

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

@Component
@Order(0)
@Profile("local")
class LocalConsumerNameExtractionFilter : Filter {
  override fun doFilter(
    request: ServletRequest,
    response: ServletResponse?,
    chain: FilterChain,
  ) {
    chain.doFilter(request.apply { setAttribute("clientName", "all-access") }, response)
  }
}
