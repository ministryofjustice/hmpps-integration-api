package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDate

data class Address(
  val country: String?,
  val county: String?,
  val endDate: LocalDate?,
  val locality: String?,
  val name: String?,
  val noFixedAddress: Boolean,
  val number: String?,
  val postcode: String?,
  val startDate: LocalDate?,
  val street: String?,
  val town: String?,
  val types: List<Type> = emptyList(),
  val notes: String?,
) {
  data class Type(
    val code: String?,
    val description: String?,
  )
}
