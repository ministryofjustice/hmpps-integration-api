package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

abstract class BaseResponse(
  open val errors: List<UpstreamApiError> = emptyList(),
) {

  fun hasError(type: UpstreamApiError.Type): Boolean = this.errors.any { it.type == type }

  fun hasErrorCausedBy(type: UpstreamApiError.Type, causedBy: UpstreamApi): Boolean =
    this.errors.any { it.type == type && it.causedBy == causedBy }
}
