package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.IOException

@Component
@Order(0)
@Profile("!local")
class ConsumerNameExtractionFilter : Filter {
  @Throws(IOException::class, ServletException::class)
  override fun doFilter(
    request: ServletRequest,
    response: ServletResponse?,
    chain: FilterChain,
  ) {
    val req = request as HttpServletRequest
    response as HttpServletResponse
    val subjectDistinguishedName = req.getHeader("subject-distinguished-name")
    val extractedConsumerName = extractConsumerName(subjectDistinguishedName)
    req.setAttribute("clientName", extractedConsumerName)
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
