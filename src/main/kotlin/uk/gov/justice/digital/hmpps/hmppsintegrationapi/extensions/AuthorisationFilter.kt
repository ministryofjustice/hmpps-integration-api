package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class AuthorisationFilter : Filter {
  @Throws(IOException::class, ServletException::class)
  override fun doFilter(
    request: ServletRequest,
    response: ServletResponse?,
    chain: FilterChain,
  ) {
    val req = request as HttpServletRequest
    println("****************** Header *******")
    println(req.getHeader("api-key-id"))

    println("****************** context Path *******")
    println(req.servletPath)

    val denylist = mapOf(
      System.getenv(MAPPS_API_KEY_ID) to listOf("/ctrlo"),
      System.getenv(CTRLO_API_KEY_ID) to listOf("/sentences"),
      System.getenv(EMILE_API_KEY_ID) to listOf("/sentences"),
      System.getenv(NHS_API_KEY_ID) to listOf("/ctrlo"),
      System.getenv(AUTOMATED_TEST_API_KEY_ID) to listOf("/ctrlo"),
    )

    val lookuptable = {
      System.getenv(MAPPS_API_KEY_ID): ["/ctrlo"]
    }

    if (lookuptable[request.getHeader("api-key-id")].contains(req.servletPath)) {
      //deny
    } else {
      //accept
    }
    chain.doFilter(request, response)
  }
}
