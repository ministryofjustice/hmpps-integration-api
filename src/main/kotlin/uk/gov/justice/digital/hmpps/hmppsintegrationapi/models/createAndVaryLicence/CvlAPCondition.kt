package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.createAndVaryLicence

class CvlAPCondition(
  val standard: List<CvlCondition>? = null,
  val bespoke: List<CvlCondition>? = null,
  val additional: List<CvlCondition>? = null,

)
