package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.web.PagedModel
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactSearchResponseItem
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedContactSearchResponse

data class PRContactSearchResponseItem(
  val id: Long,
  val firstName: String,
  val middleNames: String? = null,
  val lastName: String,
  val endDate: String? = null,
  val cityCode: String? = null,
  val flat: String? = null,
  val street: String? = null,
  val cityDescription: String? = null,
  val countryCode: String? = null,
  val property: String? = null,
  val createdTime: String? = null,
  val area: String? = null,
  val comments: String? = null,
  val postcode: String? = null,
  val mailAddress: Boolean? = false,
  val dateOfBirth: String,
  val countyDescription: String? = null,
  val noFixedAddress: Boolean? = false,
  val countyCode: String? = null,
  val countryDescription: String? = null,
  val createdBy: String? = null,
  val deceasedDate: String? = null,
  val startDate: String? = null,
) {
  fun toContact(): ContactSearchResponseItem =
    ContactSearchResponseItem(
      contactId = this.id,
      lastName = this.lastName,
      firstName = this.firstName,
      middleNames = this.middleNames,
      dateOfBirth = this.dateOfBirth,
      flat = this.flat,
      property = this.property,
      street = this.street,
      area = this.area,
      cityCode = this.cityCode,
      cityDescription = this.cityDescription,
      countyCode = this.cityCode,
      countyDescription = this.countyDescription,
      postCode = this.postcode,
      countryCode = this.countryCode,
      countryDescription = this.countryDescription,
      mailAddress = this.mailAddress,
      startDate = this.startDate,
      endDate = this.endDate,
      noFixedAddress = this.noFixedAddress,
      comments = this.comments,
    )
}

data class PRPaginatedContactSearchResponse(
  @JsonProperty("content")
  val contacts: List<PRContactSearchResponseItem>,
  @JsonProperty("page")
  val pageMetadata: PagedModel.PageMetadata,
) {
  fun toPaginatedContactSearchResponse(): PaginatedContactSearchResponse =
    PaginatedContactSearchResponse(
      content = this.contacts.map { it.toContact() },
      count = this.contacts.size,
      page = this.pageMetadata.number.toInt() + 1,
      totalCount = this.pageMetadata.totalElements,
      totalPages = this.pageMetadata.totalPages.toInt(),
      isLastPage = (this.pageMetadata.totalPages.toInt() == 0 || this.pageMetadata.number + 1 == this.pageMetadata.totalPages),
      perPage = this.pageMetadata.size.toInt(),
    )
}
