package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuthoriseConsumerService
import java.io.IOException

@Component
@Order(1)
@ConfigurationProperties(prefix = "authorisation")
class AuthorisationFilter : Filter {
  var consumers: Map<String, List<String>> = emptyMap()

  @Throws(IOException::class, ServletException::class)
  override fun doFilter(request: ServletRequest, response: ServletResponse?, chain: FilterChain) {
    val req = request as HttpServletRequest
    val res = response as HttpServletResponse
    val authoriseConsumerService = AuthoriseConsumerService()
    val subjectDistinguishedName = req.getAttribute("clientName") as String?
    val requestedPath = req.requestURI

    if (subjectDistinguishedName == null) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN, "No subject-distinguished-name header provided for authorisation")
      return
    }

    val result = authoriseConsumerService.execute(subjectDistinguishedName, consumers, requestedPath)

    if (!result) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN, "Unable to authorise $requestedPath for $subjectDistinguishedName")
      return
    }

    chain.doFilter(request, response)
  }
}
