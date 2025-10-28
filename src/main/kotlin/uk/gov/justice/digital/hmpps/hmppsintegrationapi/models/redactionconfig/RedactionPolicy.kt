package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.LimitedAccessFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.RedactionContext

interface ResponseRedaction {
  fun apply(
    redactionContext: RedactionContext,
    responseBody: Any,
  ): Any
}

data class RedactionPolicy(
  val name: String? = null,
  val responseRedactions: List<ResponseRedaction>? = null,
)

const val REDACTION_MASKING_TEXT = "**REDACTED**"

class JsonPathResponseRedaction(
  val objectMapper: ObjectMapper,
  val type: RedactionType,
  val paths: List<String>? = null,
  val includes: List<String>? = emptyList(),
  val laoOnly: Boolean = false,
) : ResponseRedaction {
  private val pathPatterns: List<Regex>? = paths?.map(::Regex)

  override fun apply(
    redactionContext: RedactionContext,
    responseBody: Any,
  ): Any {
    var shouldRun = pathPatterns?.any { it.matches(redactionContext.requestUri) } ?: true
    if (!shouldRun) return responseBody

    // check if an lao case if the policy is an lao policy and we have the hmppsId
    if (laoOnly && redactionContext.hmppsId != null) {
      val isLaoCase = redactionContext.hasAccess.getAccessFor(redactionContext.hmppsId)?.let { it.userRestricted || it.userExcluded } ?: throw LimitedAccessFailedException()
      if (!isLaoCase) {
        return responseBody
      }
    }

    val jsonString =
      when (responseBody) {
        is String -> responseBody
        else -> objectMapper.writeValueAsString(responseBody)
      }

    val doc = parseForSearch(jsonString)
    includes.orEmpty().forEach { jsonPath ->
      redactValues(jsonPath, doc)
    }
    return objectMapper.readValue(doc.jsonString(), responseBody::class.java)
  }

  fun redactValues(
    jsonPath: String,
    doc: DocumentContext,
  ) {
    for (matchedPath in allMatchingPaths(jsonPath, doc)) {
      when (type) {
        RedactionType.MASK -> doc.set(matchedPath, REDACTION_MASKING_TEXT)
        RedactionType.REMOVE -> doc.delete(matchedPath)
      }
    }
  }

  fun allMatchingPaths(
    jsonPath: String,
    doc: DocumentContext,
  ): List<String> = doc.read(JsonPath.compile(jsonPath))

  /**
   * Configure the JsonPath library to return matching paths rather than matching data.
   *
   * The jsonpath library doesn't provide a `find` function, so we have to configure the framework
   * so that `read` returns a list of matching paths rather than the matching values.
   */
  private fun searchConfiguration(): Configuration =
    Configuration
      .builder()
      .options(
        Option.AS_PATH_LIST, // Queries return a list of paths
        Option.ALWAYS_RETURN_LIST, // Return a list even if empty or single value
        Option.SUPPRESS_EXCEPTIONS, // Don't throw exception if not found
      ).build()

  fun parseForSearch(jsonText: String): DocumentContext = JsonPath.using(searchConfiguration()).parse(jsonText)!!
}

enum class RedactionType {
  REMOVE,
  MASK,
}
