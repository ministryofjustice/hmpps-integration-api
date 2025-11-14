package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig
import com.fasterxml.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.RedactionContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse

class PersonSearchResponseLaoRedaction(
  override val objectMapper: ObjectMapper,
  override val type: RedactionType,
  override val endpoints: List<String>? = null,
  override val redactions: List<String>? = emptyList(),
  override val laoOnly: Boolean = false,
) : JsonPathResponseRedaction(objectMapper, type, endpoints, redactions, laoOnly) {
  private val pathPattern: Regex = Regex("/v1/persons")

  override fun apply(
    policyName: String,
    redactionContext: RedactionContext,
    responseBody: Any,
  ): Any =
    if (pathPattern.matches(redactionContext.requestUri) && responseBody is PaginatedResponse<*>) {
      val redacted =
        responseBody.data.map {
          if (it is Person && !it.hasNoAccessRestrictions()) {
            val doc = parseForSearch(objectMapper.writeValueAsString(it))
            redactions.orEmpty().forEach { jsonPath ->
              redactValues(jsonPath, doc, redactionContext, policyName)
            }
            objectMapper.readValue(doc.jsonString(), Person::class.java)
          } else {
            it
          }
        }
      PaginatedResponse(redacted, responseBody.pagination)
    } else {
      responseBody
    }
}
