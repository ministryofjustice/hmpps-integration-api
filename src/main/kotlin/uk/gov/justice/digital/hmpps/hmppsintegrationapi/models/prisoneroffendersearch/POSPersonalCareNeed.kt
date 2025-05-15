package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonalCareNeeds

data class POSPersonalCareNeed(
  val problemType: String?,
  val problemCode: String?,
  val problemStatus: String?,
  val problemDescription: String?,
  val commentText: String?,
  val startDate: String?,
  val endDate: String?
) {
  fun toPersonalCareNeed(): PersonalCareNeeds =
    PersonalCareNeeds(
      problemType = this.problemType,
      problemCode = this.problemCode,
      problemStatus = this.problemStatus,
      problemDescription = this.problemDescription,
      commentText = this.commentText,
      startDate = this.startDate,
      endDate = this.endDate
    )
}

