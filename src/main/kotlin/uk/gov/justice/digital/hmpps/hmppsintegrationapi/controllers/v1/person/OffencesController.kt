package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.OpenAPIConfig.Companion.HMPPS_ID
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.OpenAPIConfig.Companion.PAGE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.OpenAPIConfig.Companion.PER_PAGE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetOffencesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.paginateWith

@RestController
@RequestMapping("/v1/persons")
@Tag(name = "persons")
class OffencesController(
  @Autowired val auditService: AuditService,
  @Autowired val getOffencesForPersonService: GetOffencesForPersonService,
) {
  @GetMapping("{encodedHmppsId}/offences")
  @Operation(
    summary = """
      Returns offences associated with a person, ordered by startDate (newest first).
      > Note: This API does not contain the complete list of offences for a person.
      > Offences are retrieved from Prison and Probation systems exclusively.
      > Prison systems record only custodial sentences, while Probation systems record only the main offence and some additional offences for case management purposes. Other offences recorded by HMCTS and police may not be included.
    """,
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Successfully found offences for a person with the provided HMPPS ID.",
        content = [
          Content(
            examples = [
              ExampleObject(
                """{
                  "data": [
                    {
                      "serviceSource": "NOMIS",
                      "systemSource": "PRISON_SYSTEMS",
                      "cjsCode": "RR84170",
                      "courtDates": [
                        "2018-02-10",
                        "2019-02-10"
                      ],
                      "courtName": "London Magistrates Court",
                      "description": "Commit an act / series of acts with intent to pervert the course of public justice",
                      "endDate": "2018-03-10",
                      "hoCode": 3457,
                      "startDate": "1965-12-01",
                      "statuteCode": "RR84"
                    },
                    {
                      "serviceSource": "NDELIUS",
                      "systemSource": "PROBATION_SYSTEMS",
                      "cjsCode": "RR12345",
                      "courtDates": [
                        "2020-05-15",
                        "2021-05-15"
                      ],
                      "courtName": "Manchester Crown Court",
                      "description": "Assault causing grievous bodily harm",
                      "endDate": "2020-06-20",
                      "hoCode": 3458,
                      "startDate": "2020-05-10",
                      "statuteCode": "RR85"
                    }
                  ]
                }""",
              ),
            ],
          ),
        ],
      ),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPersonOffences(
    @Parameter(ref = HMPPS_ID) @PathVariable encodedHmppsId: String,
    @Parameter(ref = PAGE) @RequestParam(required = false, defaultValue = "1", name = "page") page: Int,
    @Parameter(ref = PER_PAGE) @RequestParam(required = false, defaultValue = "10", name = "perPage") perPage: Int,
  ): PaginatedResponse<Offence> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()
    val response = getOffencesForPersonService.execute(hmppsId)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
    auditService.createEvent("GET_PERSON_OFFENCES", mapOf("hmppsId" to hmppsId))
    return response.data.paginateWith(page, perPage)
  }
}
