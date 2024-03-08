package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(3)
class ServiceDownFilter : Filter {
  override fun doFilter(
    request: ServletRequest?,
    response: ServletResponse?,
    chain: FilterChain,
  ) {
    val res = response as HttpServletResponse
    chain.doFilter(request, response)

    if (res.status >= 500) {
      res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to complete request as an upstream service is not responding")
      return
    }
  }
}
