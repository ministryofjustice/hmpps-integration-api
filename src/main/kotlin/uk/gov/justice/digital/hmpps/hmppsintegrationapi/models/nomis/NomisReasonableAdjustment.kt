package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReasonableAdjustment
import java.time.LocalDate

class NomisReasonableAdjustment(
  var treatmentCode: String? = null,
  var commentText: String? = null,
  var startDate: LocalDate? = null,
  var endDate: LocalDate? = null,
  var treatmentDescription: String? = null,
) {
  fun toReasonableAdjustment(): ReasonableAdjustment {
    return ReasonableAdjustment(this.treatmentCode, this.commentText, this.startDate, this.endDate, this.treatmentDescription)
  }
}
