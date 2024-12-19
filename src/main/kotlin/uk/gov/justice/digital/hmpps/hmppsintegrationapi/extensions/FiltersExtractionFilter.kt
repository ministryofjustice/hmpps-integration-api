package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import java.io.IOException

@Component
@Order(2)
@EnableConfigurationProperties(AuthorisationConfig::class)
class FiltersExtractionFilter
  @Autowired
  constructor(
    var authorisationConfig: AuthorisationConfig,
  ) : Filter {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(
      request: ServletRequest,
      response: ServletResponse,
      chain: FilterChain,
    ) {
      val subjectDistinguishedName = request.getAttribute("clientName") as String?
      val consumerConfig: ConsumerConfig? = authorisationConfig.consumers[subjectDistinguishedName]

      if (consumerConfig == null) {
        (response as HttpServletResponse).sendError(HttpServletResponse.SC_FORBIDDEN, "TODO")
        return
      }

      val requestingConsumersFilters: ConsumerFilters? = consumerConfig.filters

      request.setAttribute("filters", requestingConsumersFilters)
      chain.doFilter(request, response)
    }
  }
