package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.globalRedactions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles

@ControllerAdvice
@ConditionalOnProperty(
  prefix = "feature-flag",
  name = ["redaction-policy-enabled"],
  havingValue = "true",
  matchIfMissing = false,
)
class RedactionResponseBodyAdvice(
  @Autowired private val objectMapper: ObjectMapper,
  @Autowired val authorisationConfig: AuthorisationConfig,
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
    if (body == null || !selectedContentType.includes(MediaType.APPLICATION_JSON)) {
      return body
    }
    val tree = objectMapper.valueToTree<ObjectNode>(body)
    val servletRequest = (request as ServletServerHttpRequest).servletRequest
    val requestUri = (request as ServletServerHttpRequest).servletRequest.requestURI
    val subjectDistinguishedName = servletRequest.getAttribute("clientName") as? String
    val redactionPolicies = getRedactionPoliciesFromRoles(subjectDistinguishedName)
    return applyPolicies(requestUri, tree, redactionPolicies)
  }

  fun applyPolicies(
    requestUri: String,
    json: ObjectNode?,
    policies: List<RedactionPolicy>?,
  ): ObjectNode? {
    val doc: DocumentContext = JsonPath.using(config).parse(json.toString())

    for (policy in policies.orEmpty()) {
      policy.responseRedactions?.forEach { redaction ->
        redaction.apply(requestUri, doc)
      }
    }

    // Return a fresh ObjectNode instead of mutating the input
    val mapper = jacksonObjectMapper()
    return mapper.valueToTree(doc.json<String>())
  }

  private fun getRedactionPoliciesFromRoles(subjectDistinguishedName: String?): List<RedactionPolicy> {
    val consumerConfig: ConsumerConfig? = authorisationConfig.consumers[subjectDistinguishedName]
    val consumersRoles = consumerConfig?.roles
    val redactionPolicies =
      globalRedactions.values +
        consumersRoles.orEmpty().flatMap { role ->
          roles[role]?.redactionPolicies.orEmpty()
        }
    return redactionPolicies
  }
}
