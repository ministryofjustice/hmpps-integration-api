package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.web.PagedModel

data class PRLinkedPrisoners(
  @JsonProperty("content")
  val prisoners: List<PRLinkedPrisoner>,
  @JsonProperty("page")
  val pageMetadata: PagedModel.PageMetadata,
)
