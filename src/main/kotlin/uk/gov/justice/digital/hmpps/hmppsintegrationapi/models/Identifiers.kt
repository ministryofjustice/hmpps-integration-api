package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonGetter
import java.time.LocalDate

data class Identifiers(
  val nomisNumber: String? = null,
  val croNumber: String? = null,
  val deliusCrn: String? = null
)