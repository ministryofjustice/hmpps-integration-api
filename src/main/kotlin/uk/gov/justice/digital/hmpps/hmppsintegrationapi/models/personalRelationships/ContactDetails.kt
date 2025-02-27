package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.interfaces.IRelationship

data class ContactDetails(
  val contact: ContactInformation,
  val relationship: Relationship,
)

data class Relationship(
  @JsonProperty("relationshipType")
  override val relationshipType: String?,
  @JsonProperty("relationshipTypeDescription")
  override val relationshipTypeDescription: String?,
  @JsonProperty("relationshipToPrisoner")
  override val relationshipToPrisoner: String?,
  @JsonProperty("relationshipToPrisonerDescription")
  override val relationshipToPrisonerDescription: String?,
  @JsonProperty("approvedVisitor")
  val approvedPrisoner: Boolean?,
  @JsonProperty("nextOfKin")
  val nextOfKin: Boolean?,
  @JsonProperty("emergencyContact")
  val emergencyContact: Boolean?,
  @JsonProperty("isRelationshipActive")
  val isRelationshipActive: Boolean?,
  @JsonProperty("currentTerm")
  val currentTerm: Boolean?,
  @JsonProperty("comments")
  val comments: String?,
) : IRelationship

data class ContactInformation(
  val contactId: String,
  val lastName: String,
  val firstName: String,
  val middleNames: String?,
  val dateOfBirth: String,
  val flat: String?,
  val property: String?,
  val street: String?,
  val area: String?,
  val cityCode: String?,
  val cityDescription: String?,
  val countyCode: String?,
  val countyDescription: String?,
  val postCode: String?,
  val countryCode: String?,
  val countryDescription: String?,
  val primaryAddress: Boolean?,
  val mailAddress: Boolean?,
  val phoneType: String?,
  val phoneTypeDescription: String?,
  val phoneNumber: String?,
  val extNumber: String?,
)
