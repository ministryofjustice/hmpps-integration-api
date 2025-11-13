package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig
import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.RedactionContext

open class JsonPathResponseRedaction(
  open val objectMapper: ObjectMapper,
  open val type: RedactionType,
  open val paths: List<String>? = null,
  open val includes: List<String>? = emptyList(),
  open val laoOnly: Boolean = false,
) : ResponseRedaction {
  private val pathPatterns: List<Regex>? = paths?.map(::Regex)

  override fun apply(
    policyName: String,
    redactionContext: RedactionContext,
    responseBody: Any,
  ): Any {
    var shouldRun = pathPatterns?.any { it.matches(redactionContext.requestUri) } ?: true
    if (!shouldRun) return responseBody

    if (laoOnly && !redactionContext.isLimitedAccessOffender()) return responseBody

    val jsonString =
      when (responseBody) {
        is String -> responseBody
        else -> objectMapper.writeValueAsString(responseBody)
      }

    val doc = parseForSearch(jsonString)
    includes.orEmpty().forEach { jsonPath ->
      redactValues(jsonPath, doc, redactionContext, policyName)
    }
    return objectMapper.readValue(doc.jsonString(), responseBody::class.java)
  }

  fun redactValues(
    jsonPath: String,
    doc: DocumentContext,
    redactionContext: RedactionContext? = null,
    policyName: String,
  ) {
    var masks = 0
    var removes = 0
    for (matchedPath in allMatchingPaths(jsonPath, doc)) {
      when (type) {
        RedactionType.MASK -> {
          doc.set(matchedPath, REDACTION_MASKING_TEXT)
          masks++
        }
        RedactionType.REMOVE -> {
          doc.delete(matchedPath)
          removes++
        }
      }
    }
    redactionContext?.trackRedaction(policyName, masks, removes)
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
