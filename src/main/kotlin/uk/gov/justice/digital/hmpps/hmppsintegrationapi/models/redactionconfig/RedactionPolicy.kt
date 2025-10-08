package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.PathNotFoundException
import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.laoDynamicRiskRedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.laoMappaDetailsRedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.laoPersonLicencesRedactionPolicy

interface ResponseRedaction {
  val type: RedactionType
  val paths: List<String>?
  val includes: List<String>?

  fun apply(
    requestUri: String,
    doc: DocumentContext,
  )
}

data class RedactionPolicy(
  val name: String? = null,
  val responseRedactions: List<ResponseRedaction>? = null,
)

private const val REDACTION_MASKING_TEXT = "*** REDACTED ***"

data class JsonPathResponseRedaction(
  override val type: RedactionType,
  override val paths: List<String>? = null,
  override val includes: List<String>? = emptyList(),
) : ResponseRedaction {
  private val log: org.slf4j.Logger = LoggerFactory.getLogger(this::class.java)

  override fun apply(
    requestUri: String,
    doc: DocumentContext,
  ) {
    val shouldRun =
      paths
        ?.map { Regex(it) }
        ?.any { regex -> regex.matches(requestUri) }
        ?: true

    if (shouldRun) {
      includes?.forEach { jsonPath ->
        try {
          when (type) {
            RedactionType.MASK ->
              doc.set(
                com.jayway.jsonpath.JsonPath
                  .compile(jsonPath),
                REDACTION_MASKING_TEXT,
              )

            RedactionType.REMOVE ->
              doc.set(
                com.jayway.jsonpath.JsonPath
                  .compile(jsonPath),
                null,
              )
          }
        } catch (_: PathNotFoundException) {
          log.warn("Unable to find redaction masking/removal path: $jsonPath")
        } catch (ex: Exception) {
          log.warn("Unexpected error while finding redaction masking/removal path: $jsonPath")
        }
      }
    }
  }
}

enum class RedactionType {
  REMOVE,
  MASK,
}

val globalRedactions =
  listOf(
    laoPersonLicencesRedactionPolicy,
    laoMappaDetailsRedactionPolicy,
    laoDynamicRiskRedactionPolicy,
  ).associateBy { it.name }
