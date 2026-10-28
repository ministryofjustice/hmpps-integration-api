package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Licence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LicenceCondition
import java.time.LocalDate

data class NDeliusLicenceConditions(
  val sentences: List<NDeliusSentenceLicenceCondition>? = emptyList(),
) {
  fun toLicences(crn: String): List<Licence>? {
    // Convert NDelius response to a response in the same shape as CVL
    return this.sentences?.flatMap { sentence ->
      sentence.licenceConditions.map { lc ->
        Licence(
          id = sentence.eventNumber?.toString() ?: "",
          offenderNumber = crn,
          status = "IN_NDELIUS_ONLY",
          typeCode = lc.mainCategory?.code,
          createdDate = lc.createdDate?.atStartOfDay().toString(),
          updatedDate = lc.updatedDate?.atStartOfDay().toString(),
          approvedDate = lc.createdDate?.atStartOfDay().toString(),
          conditions =
            listOf(
              LicenceCondition(
                type = lc.mainCategory?.description,
                code = lc.mainCategory?.code,
                category = lc.subCategory?.code,
                condition = lc.subCategory?.description,
              ),
            ),
        )
      }
    }
  }
}

data class NDeliusSentenceLicenceCondition(
  val eventNumber: Int? = null,
  val licenceConditions: List<NDeliusLicenceCondition> = emptyList(),
)

data class NDeliusLicenceCondition(
  val mainCategory: NDeliusLicenceCategory? = null,
  val subCategory: NDeliusLicenceCategory? = null,
  val createdDate: LocalDate? = null,
  val updatedDate: LocalDate? = null,
)

data class NDeliusLicenceCategory(
  val code: String? = null,
  val description: String? = null,
)
