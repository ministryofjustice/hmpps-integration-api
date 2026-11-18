package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

@Schema(description = "Update visit request")
data class UpdateVisitRequest(
  @Schema(description = "The email address of the user who actioned this request. Required and nonblank when userType is STAFF.", required = false)
  val staffEmailAddress: String? = null,
  @Schema(description = "Application method. Defaults to BY_PRISONER when omitted or null.", required = false)
  val applicationMethodType: ApplicationMethodType = ApplicationMethodType.BY_PRISONER,
  @Schema(description = "User type for user who actioned this request. Defaults to PRISONER when omitted or null.", required = false)
  val userType: UserType = UserType.PRISONER,
  @Schema(description = "Visit Room", example = "A1", required = true)
  @field:NotBlank
  val visitRoom: String,
  @Schema(description = "Visit Type", example = "SOCIAL", required = true)
  @field:NotNull
  val visitType: VisitType,
  @Schema(description = "Visit Restriction", example = "OPEN", required = true)
  @field:NotNull
  val visitRestriction: VisitRestriction,
  @Schema(description = "The date and time of the visit", example = "2018-12-01T13:45:00", required = true)
  @field:NotNull
  val startTimestamp: LocalDateTime,
  @Schema(description = "The finishing date and time of the visit", example = "2018-12-01T13:45:00", required = true)
  @field:NotNull
  val endTimestamp: LocalDateTime,
  @Schema(description = "Visit Notes - only 1 note of each type is allowed")
  @field:Valid
  val visitNotes: List<VisitNotes> = emptyList(),
  @Schema(description = "Contact associated with the visit", required = true)
  @field:Valid
  val visitContact: VisitContact,
  @Schema(description = "List of visitors associated with the visit", required = false)
  @field:Valid
  val visitors: Set<Visitor>? = setOf(),
  @Schema(description = "Additional support associated with the visit")
  @field:Valid
  val visitorSupport: VisitorSupport? = null,
) {
  fun toHmppsMessage(
    who: String,
    visitReference: String,
    actionedBy: String? = null,
  ): HmppsMessage =
    HmppsMessage(
      eventType = HmppsMessageEventType.VISIT_UPDATED,
      messageAttributes = modelToMap(visitReference, actionedBy, who),
      who = who,
    )

  private fun modelToMap(
    visitReference: String,
    actionedBy: String?,
    clientName: String?,
  ): Map<String, Any?> =
    mapOf(
      "clientName" to clientName,
      "actionedBy" to actionedBy,
      "applicationMethodType" to this.applicationMethodType.name,
      "userType" to this.userType.name,
      "visitReference" to visitReference,
      "visitRoom" to this.visitRoom,
      "visitType" to this.visitType,
      "visitRestriction" to this.visitRestriction,
      "startTimestamp" to this.startTimestamp.toString(),
      "endTimestamp" to this.endTimestamp.toString(),
      "visitNotes" to this.visitNotes.map { mapOf("type" to it.type, "text" to it.text) },
      "visitContact" to mapOf("name" to this.visitContact.name, "telephone" to this.visitContact.telephone, "email" to this.visitContact.email),
      "visitors" to this.visitors?.map { mapOf("nomisPersonId" to it.nomisPersonId, "visitContact" to it.visitContact) },
      "visitorSupport" to this.visitorSupport?.let { mapOf("description" to this.visitorSupport.description) },
    )
}
