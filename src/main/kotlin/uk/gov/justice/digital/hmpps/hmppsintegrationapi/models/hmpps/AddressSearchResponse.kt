package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class AddressSearchResponse(
  val personAddresses: List<AddressSearchResponseItem>,
)

data class AddressSearchResponseItem(
  @Schema(description = "The ID of the persons address", example = "123456")
  val hmppsId: Long? = null,
  @Schema(description = "The address details of the person")
  val address: SearchAddress,
  @Schema(description = "The match score of the persons address", example = "100")
  val matchScore: Long? = null,
)

data class SearchAddress(
  @Schema(description = "The building name of the address", example = "Burnham House")
  val buildingName: String? = null,
  @Schema(description = "The address number", example = "1")
  val addressNumber: String? = null,
  @Schema(description = "The street name of the address", example = "Church Road")
  val streetName: String? = null,
  @Schema(description = "The district of the address", example = "Clarendon Park")
  val district: String? = null,
  @Schema(description = "The town of the address", example = "Leicester")
  val town: String? = null,
  @Schema(description = "The county of the address", example = "Leicestershire")
  val county: String? = null,
  @Schema(description = "The postcode of the address", example = "LM2 1BF")
  val postcode: String? = null,
  @Schema(description = "The end date of the address", example = "2020-08-03")
  val endDate: String? = null,
  @Schema(description = "The start date of the address", example = "2020-08-03")
  val startDate: String? = null,
  @Schema(description = "The status of the address")
  val status: SearchStatus? = null,
  @Schema(description = "The type of the address")
  val type: SearchType? = null,
  @Schema(description = "Boolean representing if the abode is fixed or not", example = "false")
  val noFixedAbode: Boolean? = null,
)

data class SearchStatus(
  @Schema(description = "Status code", example = "M")
  val code: String? = null,
  @Schema(description = "Description of the status", example = "Main")
  val description: String? = null,
)

data class SearchType(
  @Schema(description = "Type code", example = "A02")
  val code: String? = null,
  @Schema(description = "Description of the type", example = "Approved Premises")
  val description: String? = null,
)
