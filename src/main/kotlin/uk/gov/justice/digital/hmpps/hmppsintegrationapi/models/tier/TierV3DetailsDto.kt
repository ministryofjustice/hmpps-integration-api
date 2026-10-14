package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.tier

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TierV3DetailsDto(
  @Schema(description = "Tier Score", example = "D")
  val tierScore: String? = null,
  @Schema(description = "Calculation Id", example = "123e4567-e89b-12d3-a456-426614174000")
  val calculationId: String? = null,
  @Schema(description = "Calculation Date Time", example = "2021-04-23T18:25:43.511")
  val calculationDate: LocalDateTime? = null,
  @Schema(description = "Calculation input data")
  val data: TierCalculationResultEntity? = null,
  @Schema(description = "Whether the tier score is provisional", example = "false")
  val provisional: Boolean? = null,
)
