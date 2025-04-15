package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.BodyMark

data class VisibleCharacteristics(
  val heightCentimetres: Int? = null,
  val weightKilograms: Int? = null,
  val hairColour: String? = null,
  val rightEyeColour: String? = null,
  val leftEyeColour: String? = null,
  val facialHair: String? = null,
  val shapeOfFace: String? = null,
  val build: String? = null,
  val shoeSize: Int? = null,
  val tattoos: List<BodyMark>? = null,
  val scars: List<BodyMark>? = null,
  val marks: List<BodyMark>? = null,
)
