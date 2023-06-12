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
  val types: List<Type> = emptyList(),
  val type: String?,
){
  data class Type(
    val code: String,
    val description: String
  )
}
