package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuthoriseConsumerService
import java.io.IOException

@Component
@Order(1)
@EnableConfigurationProperties(AuthorisationConfig::class)
class AuthorisationFilter
  @Autowired
  constructor(
    var authorisationConfig: AuthorisationConfig,
    var authoriseConsumerService: AuthoriseConsumerService,
  ) : Filter {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(
      request: ServletRequest,
      response: ServletResponse?,
      chain: FilterChain,
    ) {
      val req = request as HttpServletRequest
      val res = response as HttpServletResponse
      val subjectDistinguishedName = req.getAttribute("clientName") as String?
      val requestedPath = req.requestURI

      if (subjectDistinguishedName == null) {
        res.sendError(HttpServletResponse.SC_FORBIDDEN, "No subject-distinguished-name header provided for authorisation")
        return
      }

      val result = authoriseConsumerService.execute(subjectDistinguishedName, authorisationConfig.consumers, requestedPath)

      if (!result) {
        res.sendError(HttpServletResponse.SC_FORBIDDEN, "Unable to authorise $requestedPath for $subjectDistinguishedName")
        return
      }

      chain.doFilter(request, response)
    }
  }
