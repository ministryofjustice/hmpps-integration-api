package uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService.IdentifierType

class CprResultException(
  val identifierType: IdentifierType,
  val hmppsId: String,
  val multipleIds: List<String> = emptyList(),
) : RuntimeException("${if (multipleIds.isNotEmpty()) "Multiple" else "No"} $identifierType found for $hmppsId in core person record.${if (multipleIds.isNotEmpty()) " " + multipleIds.joinToString(", ") else ""}")
