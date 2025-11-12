package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.Option
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LaoIdentifiable
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.RedactionContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse

@ControllerAdvice
class RedactionResponseBodyAdvice(
  val authorisationConfig: AuthorisationConfig,
  val accessFor: GetCaseAccess,
  val telemetryService: TelemetryService,
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
    val requestUri = servletRequest.requestURI
    val subjectDistinguishedName = servletRequest.getAttribute("clientName") as? String
    val redactionPolicies = getRedactionPoliciesFromRoles(subjectDistinguishedName)
    val hmppsId = servletRequest.getAttribute("hmppsId") as? String
    val redactionContext = RedactionContext(requestUri, accessFor, telemetryService, hmppsId, subjectDistinguishedName)
    return if (body is PaginatedResponse<*> && hmppsId == null) {
      // If the response is paginated and there is no hmpps id for which to apply an LAO check
      applyPoliciesPaginated(redactionContext, body, redactionPolicies)
    } else {
      applyPolicies(redactionContext, body, redactionPolicies)
    }
  }

  fun applyPolicies(
    redactionContext: RedactionContext,
    responseBody: Any,
    policies: List<RedactionPolicy>?,
  ): Any? {
    var redactedBody = responseBody
    for (policy in policies.orEmpty()) {
      policy.responseRedactions?.forEach { redaction ->
        redactedBody = redaction.apply(policy.name, redactionContext, redactedBody)
      }
    }
    return redactedBody
  }

  /**
   * Apply the redaction for each entry within the data of a Paginated response.
   * The LAO override will be set here if it is possible to get the LAO status from each entry
   *
   */
  fun applyPoliciesPaginated(
    redactionContext: RedactionContext,
    responseBody: PaginatedResponse<*>,
    policies: List<RedactionPolicy>?,
  ): Any? {
    val redacted =
      responseBody.data.map { entry ->
        if (entry is LaoIdentifiable) {
          applyPolicies(redactionContext.copy(laoOverride = entry.isLao()), entry, policies)
        } else {
          entry
        }
      }
    return PaginatedResponse(data = redacted, pagination = responseBody.pagination)
  }

  private fun getRedactionPoliciesFromRoles(subjectDistinguishedName: String?): List<RedactionPolicy> =
    buildList {
      val consumerRoles = authorisationConfig.consumers[subjectDistinguishedName]?.roles.orEmpty()
      consumerRoles.forEach { role ->
        addAll(roles[role]?.redactionPolicies.orEmpty())
      }
    }
}
