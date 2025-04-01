package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

@Schema(description = "Private prison visit request")
data class CreateVisitRequest(
  @Schema(description = "Prisoner Id", example = "AF34567G", required = true)
  @field:NotBlank
  val prisonerId: String,
  @Schema(description = "Prison Id", example = "3-character code, example = MDI", required = true)
  @field:NotBlank
  val prisonId: String,
  @Schema(description = "Client visit reference", example = "Reference ID in the client system", required = true)
  @field:NotBlank
  val clientVisitReference: String,
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
  @Schema(description = "Visit Notes")
  @field:Valid
  val visitNotes: List<VisitNotes> = emptyList(),
  @Schema(description = "Contact associated with the visit", required = true)
  @field:Valid
  val visitContact: VisitContact,
  @Schema(description = "The date and time of when the visit was created in NEXUS", example = "2018-12-01T13:45:00", required = false)
  val createDateTime: LocalDateTime? = null,
  @Schema(description = "List of visitors associated with the visit", required = false)
  @field:Valid
  val visitors: Set<Visitor>? = setOf(),
  @Schema(description = "Additional support associated with the visit")
  @field:Valid
  val visitorSupport: VisitorSupport? = null,
) {
  fun toHmppsMessage(who: String): HmppsMessage =
    HmppsMessage(
      eventType = HmppsMessageEventType.VISIT_CREATED,
      messageAttributes = modelToMap(who),
      who = who,
    )

  private fun modelToMap(clientName: String): Map<String, Any?> =
    mapOf(
      "prisonerId" to this.prisonerId,
      "prisonId" to this.prisonId,
      "clientName" to clientName,
      "clientVisitReference" to this.clientVisitReference,
      "visitRoom" to this.visitRoom,
      "visitType" to this.visitType,
      "visitRestriction" to this.visitRestriction,
      "startTimestamp" to this.startTimestamp.toString(),
      "endTimestamp" to this.endTimestamp.toString(),
      "visitNotes" to this.visitNotes.map { mapOf("type" to it.type, "text" to it.text) },
      "visitContact" to mapOf("name" to this.visitContact.name, "telephone" to this.visitContact.telephone, "email" to this.visitContact.email),
      "createDateTime" to this.createDateTime.toString(),
      "visitors" to this.visitors?.map { mapOf("nomisPersonId" to it.nomisPersonId, "visitContact" to it.visitContact) },
      "visitorSupport" to this.visitorSupport?.let { mapOf("description" to this.visitorSupport.description) },
    )
}
