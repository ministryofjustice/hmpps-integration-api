package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AddressSearchResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AddressSearchResponseItem
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SearchAddress
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SearchStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SearchType

data class PSAddressSearchResponse(
  val personAddresses: List<PSPersonAddress>,
) {
  fun toDownstreamFormat(): AddressSearchResponse =
    AddressSearchResponse(
      personAddresses =
        this.personAddresses.map { (person, address, matchScore) ->
          AddressSearchResponseItem(
            hmppsId = person.crn?.toLong(),
            address =
              SearchAddress(
                buildingName = address.buildingName,
                addressNumber = address.addressNumber,
                streetName = address.streetName,
                district = address.streetName,
                town = address.town,
                county = address.county,
                postcode = address.postcode,
                startDate = address.startDate,
                status =
                  SearchStatus(
                    code = address.status.code,
                    description = address.status.description,
                  ),
                type =
                  SearchType(
                    code = address.type.code,
                    description = address.type.description,
                  ),
                noFixedAbode = address.noFixedAbode,
              ),
            matchScore = matchScore,
          )
        },
    )
}

data class PSPersonAddress(
  val person: PSPerson,
  val address: PSAddress,
  val matchScore: Long? = null,
)

data class PSPerson(
  val id: Long? = null,
  val crn: String? = null,
  val dob: String? = null,
  val gender: String? = null,
)

data class PSAddress(
  val id: Long? = null,
  val buildingName: String? = null,
  val addressNumber: String? = null,
  val streetName: String? = null,
  val district: String? = null,
  val town: String? = null,
  val county: String? = null,
  val postcode: String? = null,
  val startDate: String? = null,
  val notes: String? = null,
  val createdDateTime: String? = null,
  val lastUpdatedDateTime: String? = null,
  val status: PSStatus,
  val type: PSType,
  val noFixedAbode: Boolean? = null,
)

data class PSStatus(
  val code: String? = null,
  val description: String? = null,
)

data class PSType(
  val code: String? = null,
  val description: String? = null,
)
