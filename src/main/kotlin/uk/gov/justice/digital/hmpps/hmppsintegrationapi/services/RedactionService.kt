package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionPolicy

@Component
class RedactionService {
  private val config =
    Configuration
      .builder()
      .options(Option.DEFAULT_PATH_LEAF_TO_NULL)
      .build()

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
}
