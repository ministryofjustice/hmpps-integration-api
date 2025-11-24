package uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService.IdentifierType

class CprResultException(
  val identifierType: IdentifierType,
  val hmppsId: String,
  val hasMultiple: Boolean = false,
) : RuntimeException("${if (hasMultiple) "Multiple" else "No"} $identifierType found for $hmppsId in core person record")
