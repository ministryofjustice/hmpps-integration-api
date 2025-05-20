package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Language

data class POSLanguage(
  val type: String?,
  val code: String?,
  val readSkill: String?,
  val writeSkill: String?,
  val speakSkill: String?,
  val interpreterRequested: Boolean?,
) {
  fun toLanguage(): Language =
    Language(
      type = this.type,
      code = this.code,
      readSkill = this.readSkill,
      writeSkill = this.writeSkill,
      speakSkill = this.speakSkill,
      interpreterRequested = this.interpreterRequested,
    )
}
