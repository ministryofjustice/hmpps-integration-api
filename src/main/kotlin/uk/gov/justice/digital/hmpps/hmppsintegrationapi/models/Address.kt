package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

data class Address(
  val country: String?,
  val county: String?,
  val endDate: String?,
  val locality: String?,
  val name: String?,
  val number: String?,
  val postcode: String?,
  val startDate: String?,
  val street: String?,
  val town: String?,
  val type: String?,
)
