package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.Option
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
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

    val servletRequest =
      (request as ServletServerHttpRequest).servletRequest.apply {
        (getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<String, String>)
          ?.get("encodedHmppsId")
          ?.let { setAttribute("encodedHmppsId", it) }
      }
    val requestUri = servletRequest.requestURI
    val subjectDistinguishedName = servletRequest.getAttribute("clientName") as? String
    val redactionPolicies = getRedactionPoliciesFromRoles(subjectDistinguishedName)

    return when (body) {
      is DataResponse<*> -> applyPolicies(requestUri, body, redactionPolicies)
      is PaginatedResponse<*> -> applyPolicies(requestUri, body, redactionPolicies)
      else -> body
    }
  }

  fun applyPolicies(
    requestUri: String,
    responseBody: Any,
    policies: List<RedactionPolicy>?,
  ): Any? {
    var redactedBody = responseBody
    for (policy in policies.orEmpty()) {
      policy.responseRedactions?.forEach { redaction ->
        redactedBody = redaction.apply(requestUri, redactedBody)
      }
    }
    return redactedBody
  }

  private fun getRedactionPoliciesFromRoles(subjectDistinguishedName: String?): List<RedactionPolicy> =
    buildList {
      addAll(globalRedactions.values)
      val consumerRoles = authorisationConfig.consumers[subjectDistinguishedName]?.roles.orEmpty()
      consumerRoles.forEach { role ->
        addAll(roles[role]?.redactionPolicies.orEmpty())
      }
    }
}
