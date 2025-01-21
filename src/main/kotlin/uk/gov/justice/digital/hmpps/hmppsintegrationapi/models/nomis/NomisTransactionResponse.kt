package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

data class NomisTransactionResponse(
  val id: String?,
  val description: String?,
) {
  fun toMap(): Map<String, String?> =
    mapOf(
      "id" to id,
      "description" to description,
    )
}
