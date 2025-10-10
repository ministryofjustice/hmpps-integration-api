package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.interfaces.IPaginatedObject
import java.time.ZonedDateTime

data class ContactEvent(
  @Schema(description = "The nDelius generated id for the contact record", example = "123456")
  val contactEventIdentifier: Long,
  @Schema(description = "The nDelius identifier (crn) for the contact record", example = "A123456")
  val offenderHmppsId: String,
  @Schema(description = "The date and time (with Europe/London timezone offset) when the record was created in nDelius", example = "2025-09-05T16:45:03.569+01:00")
  val creationDateTime: ZonedDateTime,
  @Schema(description = "The date and time (with Europe/London timezone offset) when the record was last updated in nDelius", example = "2025-09-05T16:45:03.569+01:00")
  val updateDateTime: ZonedDateTime,
  @Schema(description = "The date and time (with Europe/London timezone offset) of the contact", example = "2025-09-05T16:00:00.000+01:00")
  val contactDateTime: ZonedDateTime,
  @Schema(description = "The type of contact", example = "AP Referral")
  val contactType: String,
  @Schema(description = "The outcome of that contact", required = false, example = "Referral Cancelled")
  val outcome: String? = null,
  @Schema(description = "The location area of that contact", example = "Gloucester")
  val location: String?,
  @Schema(description = "The probation area of that contact", example = "South West")
  val area: String,
  @Schema(description = "The probation area of that contact", example = "Public Protection Residential")
  val pdu: String,
  @Schema(description = "The nDelius identifier for the team from where the contact originated", example = "N56DTX")
  val teamId: String,
  @Schema(description = "The name of the team from where the contact originated", example = "Approved Premises")
  val teamName: String,
  @Schema(description = "The nDelius identifier of the officer from where the contact originated", example = "N56A070")
  val officerId: String,
  @Schema(description = "The name of the officer from where the contact originated", example = "Jane Smith")
  val officerName: String,
  @Schema(description = "The description of the contact", example = "Approved Premises Referral")
  val description: String,
  @Schema(description = "The notes (if any) attached to the contact", example = "The referral has been cancelled")
  val notes: String?,
)

data class ContactEvents(
  override val content: List<ContactEvent>,
  override val isLastPage: Boolean,
  override val count: Int,
  override val page: Int,
  override val perPage: Int,
  override val totalCount: Long,
  override val totalPages: Int,
) : IPaginatedObject<ContactEvent>
