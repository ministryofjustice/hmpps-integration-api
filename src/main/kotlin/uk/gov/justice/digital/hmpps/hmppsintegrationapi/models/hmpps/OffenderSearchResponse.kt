package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonIgnore

interface OffenderSearchResponse {
  @JsonIgnore
  fun isRedirectionResponse(): Boolean = false
  fun offenderSearchResult(): OffenderSearchResult? = null
  fun offenderSearchRedirectionResult(): OffenderSearchRedirectionResult? = null
}

data class OffenderSearchResult(
  val prisonerOffenderSearch: Person? = null,
  val probationOffenderSearch: PersonOnProbation? = null,
) : OffenderSearchResponse {
  override fun offenderSearchResult(): OffenderSearchResult? {
    return this
  }
}
data class OffenderSearchRedirectionResult(
  val prisonerNumber: String? = null,
  val removePrisonerNumber: String? = null,
  @JsonIgnore val redirectUrl: String? = null,
) : OffenderSearchResponse {
  override fun isRedirectionResponse(): Boolean {
    return true
  }
  override fun offenderSearchRedirectionResult(): OffenderSearchRedirectionResult? {
    return this
  }
}
