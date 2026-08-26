package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr

data class CorePersonRecordSearchRequest(
  val firstName: String? = null,
  val lastName: String? = null,
  val middleName: String? = null,
  val dateOfBirth: String? = null,
  val firstNameAliases: List<String>? = null,
  val lastNameAliases: List<String>? = null,
  val dateOfBirthAliases: List<String>? = null,
  val postcodes: List<String>? = null,
) {
  fun toMap(): Map<String, Any?> =
    mapOf(
      "firstName" to firstName,
      "lastName" to lastName,
      "middleName" to middleName,
      "dateOfBirth" to dateOfBirth,
      "firstNameAliases" to firstNameAliases,
      "lastNameAliases" to lastNameAliases,
      "dateOfBirthAliases" to dateOfBirthAliases,
      "postcodes" to postcodes,
    )

  fun toAuditableMap(): Map<String, String?> =
    mapOf(
      "firstName" to firstName,
      "lastName" to lastName,
      "middleName" to middleName,
      "dateOfBirth" to dateOfBirth,
    )
}
