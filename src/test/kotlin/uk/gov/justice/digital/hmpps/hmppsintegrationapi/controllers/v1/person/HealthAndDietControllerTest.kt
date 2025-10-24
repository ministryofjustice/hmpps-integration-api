package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.WebMvcTestConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CateringInstruction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Diet
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.FoodAllergy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HealthAndDiet
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.MedicalDietaryRequirement
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonalisedDietaryRequirement
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetHealthAndDietService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [HealthAndDietController::class])
@Import(WebMvcTestConfiguration::class)
@ActiveProfiles("test")
internal class HealthAndDietControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getHealthAndDietService: GetHealthAndDietService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec(
    {
      val hmppsId = "A1234AA"
      val path = "/v1/persons/$hmppsId/health-and-diet"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)
      val filters = null

      describe("GET $path") {
        beforeTest {
          Mockito.reset(auditService)
          Mockito.reset(getHealthAndDietService)
          whenever(getHealthAndDietService.execute(hmppsId, filters))
            .thenReturn(
              Response(
                data =
                  HealthAndDiet(
                    diet =
                      Diet(
                        foodAllergies = listOf(FoodAllergy(id = "", code = "", description = "")),
                        medicalDietaryRequirements = listOf(MedicalDietaryRequirement(id = "", code = "", description = "")),
                        personalisedDietaryRequirements = listOf(PersonalisedDietaryRequirement(id = "", code = "", description = "")),
                        cateringInstructions = CateringInstruction(value = ""),
                      ),
                    smoking = "Y",
                  ),
              ),
            )
        }

        it("should respond with status ok") {
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("should call get health and diet information service") {
          mockMvc.performAuthorised(path)
          verify(getHealthAndDietService, VerificationModeFactory.times(1)).execute(hmppsId, filters)
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)

          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("GET_PERSON_HEALTH_AND_DIET_INFORMATION", mapOf("hmppsId" to hmppsId))
        }

        it("returns health and dietary information for a person with matching id") {
          val result = mockMvc.performAuthorised(path)
          result.response.contentAsString.shouldContain(
            """
            {
              "data": {
                "diet": {
                  "foodAllergies": [
                    {
                      "id": "",
                      "code": "",
                      "description": "",
                      "comment": null
                    }
                  ],
                  "medicalDietaryRequirements": [
                    {
                      "id": "",
                      "code": "",
                      "description": "",
                      "comment": null
                    }
                  ],
                  "personalisedDietaryRequirements": [
                    {
                      "id": "",
                      "code": "",
                      "description": "",
                      "comment": null
                    }
                  ],
                  "cateringInstructions": {
                    "value": ""
                  }
                },
                "smoking": "Y"
              }
            }
          """.removeWhitespaceAndNewlines(),
          )
        }

        it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
          whenever(getHealthAndDietService.execute(hmppsId, filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("returns a 400 BAD Request status code when an invalid hmpps id is found in the upstream API") {
          whenever(getHealthAndDietService.execute(hmppsId, filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PRISON_API,
                    type = UpstreamApiError.Type.BAD_REQUEST,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }
      }
    },
  )
