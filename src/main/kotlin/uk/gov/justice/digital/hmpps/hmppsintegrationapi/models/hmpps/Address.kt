package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class Address(
  @Schema(example = "England")
  val country: String?,
  @Schema(example = "Greater London")
  val county: String?,
  @Schema(example = "20 May 2023")
  val endDate: LocalDate?,
  @Schema(example = "London Bridge")
  val locality: String?,
  @Schema(example = "Name of the building of residence")
  val name: String?,
  @Schema(description = "Indicates whether the person has a permanent place of residence", example = "true")
  val noFixedAddress: Boolean,
  @Schema(example = "1")
  val number: String?,
  @Schema(example = "SE1 1TE")
  val postcode: String?,
  @Schema(example = "1 January 2023")
  val startDate: LocalDate?,
  @Schema(example = "O'Meara Street")
  val street: String?,
  @Schema(example = "London")
  val town: String?,
  val types: List<Type> = emptyList(),
  @Schema(example = "This is their partner's address.")
  val notes: String?,
) {
  @Schema(description = "Type or usage of address, for example `Business Address`, `Home Address`, `Work Address`.")
  data class Type(
    @Schema(example = "BUS", description = "Address type code, for example: `BUS`, `HOME`, `WORK`.")
    val code: String?,
    @Schema(example = "Business Address", description = "Description of address type, for example: `Business Address`, `Home Address`, `Work Address`.")
    val description: String?,
  )
}
