package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.Option
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.Series.SUCCESSFUL
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.LimitedAccessException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.LimitedAccessFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse

@ControllerAdvice
@ConditionalOnProperty(
  prefix = "feature-flag",
  name = ["redaction-policy-enabled"],
  havingValue = "true",
  matchIfMissing = false,
)
class RedactionResponseBodyAdvice(
  val authorisationConfig: AuthorisationConfig,
  val globalRedactions: Map<String, RedactionPolicy>,
  val accessFor: GetCaseAccess,
) : ResponseBodyAdvice<Any> {
  val config: Configuration =
    Configuration
      .builder()
      .options(Option.DEFAULT_PATH_LEAF_TO_NULL)
      .build()

  override fun supports(
    returnType: MethodParameter,
    converterType: Class<out HttpMessageConverter<*>?>,
  ): Boolean {
    // apply to JSON responses only
    return true
  }

  override fun beforeBodyWrite(
    body: Any?,
    returnType: MethodParameter,
    selectedContentType: MediaType,
    selectedConverterType: Class<out HttpMessageConverter<*>?>,
    request: ServerHttpRequest,
    response: ServerHttpResponse,
  ): Any? {
    if (body == null) return null
    if (!selectedContentType.includes(MediaType.APPLICATION_JSON)) return body

    if (HttpStatus.Series.valueOf((response as ServletServerHttpResponse).servletResponse.status) != SUCCESSFUL) {
      return body
    }

    val servletRequest =
      (request as ServletServerHttpRequest).servletRequest.apply {
        (getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<String, String>)
          ?.get("hmppsId")
          ?.let { setAttribute("hmppsId", it) }
      }

    val subjectDistinguishedName = servletRequest.getAttribute("clientName") as? String
    val redactionPolicies = getRedactionPoliciesFromRoles(subjectDistinguishedName, servletRequest)

    return when (body) {
      is DataResponse<*> -> applyPolicies(servletRequest, body, redactionPolicies)
      is PaginatedResponse<*> -> applyPolicies(servletRequest, body, redactionPolicies)
      else -> body
    }
  }

  fun applyPolicies(
    request: HttpServletRequest,
    responseBody: Any,
    policies: List<RedactionPolicy>?,
  ): Any? {
    var redactedBody = responseBody
    for (policy in policies.orEmpty()) {
      policy.responseRedactions?.forEach { redaction ->
        redactedBody = redaction.apply(request.requestURI, redactedBody)
      }
    }
    return redactedBody
  }

  private fun getRedactionPoliciesFromRoles(
    subjectDistinguishedName: String?,
    request: HttpServletRequest,
  ): List<RedactionPolicy> =
    buildList {
      addAll(globalRedactions.values)
      val consumerRoles = authorisationConfig.consumers[subjectDistinguishedName]?.roles.orEmpty()

      consumerRoles.forEach { role ->
        // Add lao polices
        addAll(laoRedactionPolicies(roles[role]?.laoRejectionPolicies() ?: emptyList(), request))
        // Add standard policies
        addAll(roles[role]?.standardRedactionPolices() ?: emptyList())
      }
    }

  private fun laoRedactionPolicies(
    list: List<RedactionPolicy>,
    request: HttpServletRequest,
  ): List<RedactionPolicy> =
    list.map {
      when (it.endpoints.any { Regex(it).matches(request.requestURI) } && it.reject) {
        true -> {
          val hmppsId = request.getAttribute("hmppsId") as? String
          if (isLao(hmppsId) == true) throw LimitedAccessException() else it
        }
        else -> it
      }
    }

  fun isLao(hmppsId: String?): Boolean? =
    hmppsId?.let {
      accessFor.getAccessFor(hmppsId)?.let { it.userRestricted || it.userExcluded } ?: throw LimitedAccessFailedException()
    }
}
