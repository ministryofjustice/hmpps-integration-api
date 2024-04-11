package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

class PersonProtectedCharacteristics(
  val age: Number,
  val gender: String?,
  val sexualOrientation: String?,
  val ethnicity: String?,
  val nationality: String?,
  val religion: String?,
  val disabilities: List<Disability>,
  var maritalStatus: String? = null,
  var reasonableAdjustments: List<ReasonableAdjustment> = emptyList(),
)
