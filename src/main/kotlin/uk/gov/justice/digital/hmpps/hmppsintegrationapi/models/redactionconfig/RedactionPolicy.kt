package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor.Redactor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse

interface ResponseRedaction {
  fun apply(
    requestUri: String,
    responseBody: Any,
  ): Any
}

data class RedactionPolicy(
  val name: String? = null,
  val responseRedactions: List<ResponseRedaction>? = null,
)

const val REDACTION_MASKING_TEXT = "**REDACTED**"

@Suppress("UNCHECKED_CAST")
class DelegatingResponseRedaction<T : Any>(
  val redactor: Redactor<T>,
  val paths: List<String>?,
) : ResponseRedaction {
  override fun apply(
    requestUri: String,
    responseBody: Any,
  ): Any {
    val shouldApply = paths == null || paths.any { Regex(it).matches(requestUri) }

    if (!shouldApply) return responseBody

    return when (responseBody) {
      is DataResponse<*> -> redactDataResponse(responseBody)
      is PaginatedResponse<*> -> redactPaginatedResponse(responseBody)
      else -> responseBody
    }
  }

  private fun applyRedactors(value: Any?): Any? {
    if (value == null) return null
    val redactorForType = redactor.takeIf { it.type.isInstance(value) } ?: return value
    @Suppress("UNCHECKED_CAST")
    return (redactorForType as Redactor<Any>).redact(value)
  }

  private fun <T> redactDataResponse(response: DataResponse<T>): DataResponse<T> {
    val redactedData = applyRedactors(response.data)
    @Suppress("UNCHECKED_CAST")
    return response.copy(data = redactedData as T)
  }

  private fun <T> redactPaginatedResponse(response: PaginatedResponse<T>): PaginatedResponse<T> {
    val redactedList = response.data.map { applyRedactors(it) }
    @Suppress("UNCHECKED_CAST")
    return response.copy(data = redactedList as List<T>, pagination = response.pagination)
  }
}

class JsonPathResponseRedaction(
  val objectMapper: ObjectMapper,
  val type: RedactionType,
  val paths: List<String>? = null,
  val includes: List<String>? = emptyList(),
) : ResponseRedaction {
  private val log: org.slf4j.Logger = LoggerFactory.getLogger(this::class.java)
  val config: Configuration =
    Configuration
      .builder()
      .options(
        Option.AS_PATH_LIST, // Queries return a list of paths
        Option.ALWAYS_RETURN_LIST, // Return a list even if empty or single value
        Option.SUPPRESS_EXCEPTIONS) // Don't throw exception if not found
      .build()

  private val pathPatterns: List<Regex>? = paths?.map(::Regex)

  override fun apply(
    requestUri: String,
    responseBody: Any,
  ): Any {
    val shouldRun = pathPatterns?.any { it.matches(requestUri) } ?: true
    if (!shouldRun) return responseBody

    val jsonString =
      when (responseBody) {
        is String -> responseBody
        else -> objectMapper.writeValueAsString(responseBody)
      }

    val doc = parse(jsonString)
    includes.orEmpty().forEach { jsonPath ->
      redactValues(jsonPath, doc)
    }
    return objectMapper.readValue(doc.jsonString(), responseBody::class.java)
  }

  fun redactValues(jsonPath: String, doc: DocumentContext) {
    for (matchedPath in allMatchingPaths(jsonPath, doc)) {
      when (type) {
        RedactionType.MASK -> doc.set(matchedPath, REDACTION_MASKING_TEXT)
        RedactionType.REMOVE -> doc.delete(matchedPath)
      }
    }
  }

  fun allMatchingPaths(jsonPath: String, doc: DocumentContext) : List<String> = doc.read(JsonPath.compile(jsonPath))

  fun parse(jsonText: String) : DocumentContext = JsonPath.using(config).parse(jsonText)!!
}

enum class RedactionType {
  REMOVE,
  MASK,
}
