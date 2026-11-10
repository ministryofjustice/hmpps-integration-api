package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Licence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LicenceCondition
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class NDeliusLicenceConditions(
  val sentences: List<NDeliusSentenceLicenceCondition>? = emptyList(),
) {
  fun toLicences(crn: String): List<Licence>? {
    // Convert NDelius response to a response in the same shape as CVL
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    return this.sentences?.flatMap { sentence ->
      sentence.licenceConditions.map { lc ->
        Licence(
          id = sentence.eventNumber?.toString() ?: "",
          offenderNumber = crn,
          status = "IN_NDELIUS_ONLY",
          typeCode = "AP",
          createdDate = lc.createdDate?.atStartOfDay()?.format(formatter),
          updatedDate = lc.updatedDate?.atStartOfDay()?.format(formatter),
          approvedDate = lc.createdDate?.atStartOfDay()?.format(formatter),
          conditions =
            listOf(
              LicenceCondition(
                type = "Bespoke",
                code = lc.subCategory?.code,
                category = lc.mainCategory?.description,
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
