package uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SystemSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import java.time.LocalDate

fun generateTestOffence(
  serviceSource: UpstreamApi = UpstreamApi.NDELIUS,
  systemSource: SystemSource = SystemSource.PROBATION_SYSTEMS,
  cjsCode: String? = "RR12345",
  hoCode: String? = "05800",
  description: String? = "Some description",
  startDate: LocalDate? = LocalDate.parse("2020-02-03"),
  endDate: LocalDate? = LocalDate.parse("2020-03-03"),
  courtDates: List<LocalDate?> = listOf(LocalDate.parse("2020-04-03")),
  statuteCode: String? = "RR12",
): Offence = Offence(
  serviceSource = serviceSource,
  systemSource = systemSource,
  cjsCode = cjsCode,
  hoCode = hoCode,
  description = description,
  startDate = startDate,
  endDate = endDate,
  courtDates = courtDates,
  statuteCode = statuteCode,
)
