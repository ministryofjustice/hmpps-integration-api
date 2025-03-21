package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.LaoConfigurationException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.CaseAccess

@Component
class LaoContextConfiguration(
  private val laoContextInterceptor: LaoContextInterceptor,
) : WebMvcConfigurer {
  override fun addInterceptors(registry: InterceptorRegistry) {
    registry
      .addInterceptor(laoContextInterceptor)
      .addPathPatterns(*laoContextInterceptor.paths.toTypedArray())
  }
}

@Component
class LaoContextInterceptor(
  private val crnSupplier: CrnSupplier,
  private val loaChecker: AccessFor,
) : HandlerInterceptor {
  val paths: List<String> =
    listOf(
      "/v1/persons/*/status-information",
      "/v1/persons/*/risks/mappadetail",
      "/v1/persons/*/risks/dynamic",
      "/v1/persons/*/licences/conditions",
      "/v1/persons/*/risks/serious-harm",
      "/v1/persons/*/risks/risk-management-plan",
    )

  private val regexes =
    paths.map {
      it.replace("*", "(?<$HMPPS_ID_NAME>.*)").toRegex()
    }

  override fun preHandle(
    request: HttpServletRequest,
    response: HttpServletResponse,
    handler: Any,
  ): Boolean {
    val hmppsId = request.extractHmppsId()
    crnSupplier
      .getCrn(hmppsId)
      ?.let {
        loaChecker.getAccessForCrn(it)?.asLaoContext()
      }?.also { request.setAttribute(LaoContext::class.simpleName, it) }
    return true
  }

  private fun HttpServletRequest.extractHmppsId(): String =
    regexes
      .firstOrNull {
        pathInfo.matches(it)
      }?.find(pathInfo)
      ?.groups
      ?.get(HMPPS_ID_NAME)
      ?.value
      ?.decodeUrlCharacters()
      ?: throw LaoConfigurationException(pathInfo)

  companion object {
    const val HMPPS_ID_NAME = "hmppsId"
  }
}

private fun CaseAccess.asLaoContext() = LaoContext(crn, userExcluded, userRestricted, exclusionMessage, restrictionMessage)
