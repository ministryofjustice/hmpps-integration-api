package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonIgnore

sealed interface OffenderSearchResponse

data class OffenderSearchResult(
  val prisonerOffenderSearch: Person? = null,
  val probationOffenderSearch: PersonOnProbation? = null,
) : OffenderSearchResponse

data class OffenderSearchRedirectionResult(
  val prisonerNumber: String? = null,
  val removePrisonerNumber: String? = null,
  @JsonIgnore val redirectUrl: String? = null,
) : OffenderSearchResponse
