package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.MappaCategory

@JsonIgnoreProperties(ignoreUnknown = true)
data class HmppsDomainEvent(
  @JsonProperty("eventType") val eventType: String,
  @JsonProperty("occurredAt") val occurredAt: String,
  @JsonProperty("personReference") val personReference: PersonReference?,
  @JsonProperty("additionalInformation") val additionalInformation: AdditionalInformation?,
  @JsonProperty("reason") val reason: String? = null,
  @JsonProperty("prisonerId") val prisonerId: String? = null,
  @JsonProperty("prisonId") val prisonId: String? = null,
) {
  fun isValidContactEvent(): Boolean = additionalInformation?.mappa?.category != null && MappaCategory.from(additionalInformation.mappa.category) != MappaCategory.UNKNOWN
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class PersonReference(
  @JsonProperty("identifiers") val identifiers: List<Identifier>,
) {
  fun findCrnIdentifier(): String? = this.identifiers.firstOrNull { it.type == "CRN" }?.value

  fun findNomsIdentifier(): String? = this.identifiers.firstOrNull { it.type == "nomsNumber" || it.type == "NOMS" }?.value
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Identifier(
  @JsonProperty("type") val type: String,
  @JsonProperty("value") val value: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Mappa(
  @JsonProperty("category") val category: Int?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AdditionalInformation(
  @JsonProperty("registerTypeDescription") val registerTypeDescription: String? = null,
  @JsonProperty("registerTypeCode") val registerTypeCode: String? = null,
  @JsonProperty("nomsNumber") val nomsNumber: String? = null,
  @JsonProperty("prisonerId") val prisonerId: String? = null,
  @JsonProperty("prisonerNumber") val prisonerNumber: String? = null,
  @JsonProperty("alertCode") val alertCode: String? = null,
  @JsonProperty("contactPersonId") val contactPersonId: String? = null,
  @JsonProperty("reference") val reference: String? = null,
  @JsonProperty("categoriesChanged") val categoriesChanged: List<String>? = emptyList(),
  @JsonProperty("key") val key: String? = null,
  @JsonProperty("prisonId") val prisonId: String? = null,
  @JsonProperty("reason") val reason: String? = null,
  @JsonProperty("removedNomsNumber") val removedNomsNumber: String? = null,
  @JsonProperty("contactId") val contactEventId: String? = null,
  @JsonProperty("mappa") val mappa: Mappa? = null,
)
