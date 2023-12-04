package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.ExtractConsumerFromSubjectDistinguishedNameService
import java.io.IOException

@Component
@ConfigurationProperties(prefix = "authorisation")
class AuthorisationFilter : Filter {
  var consumers: Map<String, List<String>> = emptyMap()

  @Throws(IOException::class, ServletException::class)

  override fun doFilter(
    request: ServletRequest,
    response: ServletResponse?,
    chain: FilterChain,
  ) {
    val req = request as HttpServletRequest

    println("****************** Header *******")
    val subjectDistinguishedName = req.getHeader("subject-distinguished-name")
    val consumer = ExtractConsumerFromSubjectDistinguishedNameService().execute(subjectDistinguishedName)
    val allowedPaths = consumers[consumer]

    println(subjectDistinguishedName)
    println(consumer)
    println(consumers)
    println(allowedPaths)
    println(req.servletPath)

    chain.doFilter(request, response)
  }
}
