package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = InductionScheduleDeserializer::class)
data class InductionSchedule(
  @Schema(
    description = "An ISO-8601 date representing when the Induction should be completed by.",
    example = "2023-09-01",
  )
  val deadlineDate: LocalDate? = null,
  @Schema(
    description = "The current status of the Induction Schedule",
    example = "SCHEDULED",
  )
  val scheduleStatus: String? = null,
  @Schema(
    description = "The Induction Schedule rule used to determine deadline date.",
    example = "NEW_PRISON_ADMISSION",
  )
  val scheduleCalculationRule: String? = null,
  @Schema(
    description = "The Nomis number of the person.",
    example = "A1234BC",
  )
  val nomisNumber: String? = null,
)

class InductionScheduleDeserializer : JsonDeserializer<InductionSchedule>() {
  override fun deserialize(
    parser: JsonParser,
    ctxt: DeserializationContext,
  ): InductionSchedule {
    val node = parser.codec.readTree<com.fasterxml.jackson.databind.JsonNode>(parser)
    val nomisNumber = node["prisonNumber"]?.asText()
    val deadlineDate = node["deadlineDate"]?.asText()?.let { LocalDate.parse(it) }
    val scheduleStatus = node["scheduleStatus"]?.asText()
    val scheduleCalculationRule = node["scheduleCalculationRule"]?.asText()

    return InductionSchedule(
      deadlineDate = deadlineDate,
      scheduleStatus = scheduleStatus,
      scheduleCalculationRule = scheduleCalculationRule,
      nomisNumber = nomisNumber,
    )
  }
}
