package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.*
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import java.io.IOException

class FiltersExtractionFilter : Filter {
  @Throws(IOException::class, ServletException::class)
  override fun doFilter(
    request: ServletRequest,
    response: ServletResponse?,
    chain: FilterChain,
  ) {
    request.setAttribute("filters", ConsumerFilters(mapOf("example-filter" to listOf("filter-1", "filter-2"))))
    chain.doFilter(request, response)
  }
}
