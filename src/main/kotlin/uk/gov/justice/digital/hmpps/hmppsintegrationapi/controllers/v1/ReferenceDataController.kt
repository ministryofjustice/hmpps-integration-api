package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import ReferenceData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ReferenceDataService

@RestController
@EnableConfigurationProperties(AuthorisationConfig::class)
@RequestMapping("/v1/hmpps/reference-data")
@Tag(name = "Reference Data")
class ReferenceDataController(
  var referenceDataService: ReferenceDataService,
) {
  @GetMapping
  @Operation(
    summary = """Returns probation and prison reference data codes descriptions for values returned by the API""",
    responses = [
      ApiResponse(
        responseCode = "200",
        useReturnTypeSchema = true,
        description = "Successfully returned prison and probation reference data.",
        content = [
          Content(
            schema = Schema(implementation = Response::class),
            examples = [
              ExampleObject(
                """{
                  "data": {
                    "prisonReferenceData": {
                      "PHONE_TYPE": [
                        {
                          "code": "a",
                          "description": "desc_a"
                        }
                      ],
                      "ALERT_TYPE": [
                        {
                          "code": "a",
                          "description": "desc_a"
                        }
                      ]
                    },
                    "probationReferenceData": {
                      "PHONE_TYPE": [
                        {
                          "code": "a",
                          "description": "desc_a"
                        }
                      ],
                      "REGISTER_TYPE": [
                        {
                          "code": "a",
                          "description": "desc_a"
                        }
                      ]
                    }
                  },
                  "errors": []
                }""",
              ),
            ],
          ),
        ],
      ),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getReferenceData(): Response<ReferenceData?> = referenceDataService.referenceData()
}
