package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import java.time.LocalDate

data class NomisReferenceCode(
  val domain: String? = null,
  val code: String? = null,
  val description: String? = null,
  val parentDomain: String? = null,
  val parentCode: String? = null,
  val activeFlag: String? = null,
  val listSeq: Number = 0,
  val systemDataFlag: String? = null,
  val expiredDate: LocalDate? = null,
  var subCodes: List<NomisReferenceCode> = emptyList(),
)
