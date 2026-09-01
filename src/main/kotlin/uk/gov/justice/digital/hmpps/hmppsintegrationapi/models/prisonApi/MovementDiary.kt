package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi

import io.swagger.v3.oas.annotations.media.Schema

data class MovementDiary(
  @Schema(example = "2023-12-08T15:50:37Z")
  val diaryDateTime: String? = null,
  @Schema(example = "PA")
  val diaryReasonCode: String? = null,
  @Schema(example = "Moved to different location")
  val diaryComments: String? = null,
)
