package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class ReferenceData(
  @Schema(
    description = "A map of prison related reference data",
    example = """
      {
        "PHONE_TYPE": [
          {
            "code": "M",
            "description": "Mobile"
          }
        ],
        "REGISTER_TYPE": [
          {
            "code": "R1",
            "description": "Register 1"
          },
          {
            "code": "R2",
            "description": "Register 2"
          }
        ]
      }
  """,
  )
  val prisonReferenceData: Map<String, List<ReferenceDataItem>>?,
  @Schema(
    description = "A map of probation related reference data",
    example = """
      {
        "GENDER": [
          {
            "code": "M",
            "description": "Male"
          },
          {
            "code": "F",
            "description": "Female"
          }
        ],
        "ALERT_TYPE": [
          {
            "code": "A1",
            "description": "Alert 1"
          }
        ]
      }
  """,
  )
  val probationReferenceData: Map<String, List<ReferenceDataItem>>?,
)

data class ReferenceDataItem(
  @Schema(description = "Reference data code", example = "M")
  val code: String,
  @Schema(description = "Reference data description", example = "Male")
  val description: String,
)
