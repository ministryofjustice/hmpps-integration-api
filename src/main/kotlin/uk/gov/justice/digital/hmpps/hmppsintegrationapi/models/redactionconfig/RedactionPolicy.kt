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

private const val REDACTION_MASKING_TEXT = "**REDACTED**"

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
      .options(Option.AS_PATH_LIST)
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

    val doc = JsonPath.using(config).parse(jsonString)
    includes.orEmpty().forEach { jsonPath ->
      try {
        if (exists(jsonPath, doc)) {
          when (type) {
//            RedactionType.MASK -> doc.set(JsonPath.compile(jsonPath), REDACTION_MASKING_TEXT)
            RedactionType.MASK -> doc.map(JsonPath.compile(jsonPath)) { _, _ -> REDACTION_MASKING_TEXT }
            RedactionType.REMOVE -> doc.delete(JsonPath.compile(jsonPath))
          }
        }
      } catch (ex: Exception) {
        log.warn("Unexpected error while applying redaction on: $jsonPath", ex)
      }
    }
    return objectMapper.readValue(doc.jsonString(), responseBody::class.java)
  }

  private fun exists(
    path: String,
    doc: DocumentContext,
  ): Boolean = runCatching { doc.read<Any?>(path) != null }.getOrDefault(false)
}

enum class RedactionType {
  REMOVE,
  MASK,
}
