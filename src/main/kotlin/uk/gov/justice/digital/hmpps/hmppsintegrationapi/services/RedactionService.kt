package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import com.jayway.jsonpath.PathNotFoundException
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType

// private val log = KotlinLogging.logger {}

@Component
class RedactionService {
  val redactionMaskingValue = "***REDACTED***"

  private val config =
    Configuration
      .builder()
      .options(Option.DEFAULT_PATH_LEAF_TO_NULL)
      .build()

  fun applyPolicies(
    json: ObjectNode?,
    policies: List<RedactionPolicy>?,
  ): ObjectNode? {
    val doc: DocumentContext = JsonPath.using(config).parse(json.toString())

    for (policy in policies.orEmpty()) {
      policy.responseRedactions?.forEach { redaction ->
        redaction.includes?.forEach { path ->
          try {
            when (redaction.type) {
              RedactionType.MASK -> doc.set(JsonPath.compile(path), redactionMaskingValue)
              RedactionType.REMOVE -> doc.delete(JsonPath.compile(path))
            }
          } catch (_: PathNotFoundException) {
//            log.warn("Path not found for redaction: $path")
          } catch (ex: Exception) {
//            log.error("Failed to apply redaction on $path", ex)
          }
        }
      }
    }

    // Return a fresh ObjectNode instead of mutating the input
    val mapper = jacksonObjectMapper()
    return mapper.valueToTree(doc.json<String>())
  }
}
