package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nonAssociation.NonAssociation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nonAssociation.NonAssociationPrisonerDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nonAssociation.NonAssociations
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonersNonAssociationsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [OffenderRestrictionsController::class])
@ActiveProfiles("test")
class OffenderRestrictionsControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getPrisonersNonAssociationsService: GetPrisonersNonAssociationsService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec({
    val prisonId = "XYZ"
    val hmppsId = "200313116M"
    val nonAssociationsPath = "/v1/prison/$prisonId/prisoners/$hmppsId/non-associations"
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)

    val nonAssociations =
      NonAssociations(
        nonAssociations =
          listOf(
            NonAssociation(
              role = "VICTIM",
              reason = "BULLYING",
              roleDescription = "Victim",
              reasonDescription = "Bullying",
              restrictionType = "CELL",
              restrictionTypeDescription = "Cell only",
              comment = "John and Luke always end up fighting",
              authorisedBy = "Unknown",
              whenCreated = "Unknown",
              whenUpdated = "Unknown",
              updatedBy = "Unknown",
              isClosed = false,
              closedBy = null,
              closedReason = null,
              closedAt = null,
              otherPrisonerDetails =
                NonAssociationPrisonerDetails(
                  prisonerNumber = "D5678EF",
                  role = "PERPETRATOR",
                  roleDescription = "Perpetrator",
                  firstName = "Joseph",
                  lastName = "Bloggs",
                  prisonId = "MDI",
                  prisonName = "Moorland (HMP & YOI)",
                  cellLocation = "B-2-007",
                ),
              isOpen = true,
              id = 42,
            ),
          ),
      )

    it("returns the correct non associations data") {
      whenever(getPrisonersNonAssociationsService.execute(hmppsId, prisonId, includeOpen = "true", includeClosed = "false", filters = null)).thenReturn(
        Response(
          data = nonAssociations,
        ),
      )
      val result = mockMvc.performAuthorised("$nonAssociationsPath?includeOpen=true&includeClosed=false")
      result.response.contentAsString.shouldContain(
        """
          {
            "data": {
              "nonAssociations": [
                {
                  "id": 42,
                  "role": "VICTIM",
                  "roleDescription": "Victim",
                  "reason": "BULLYING",
                  "reasonDescription": "Bullying",
                  "restrictionType": "CELL",
                  "restrictionTypeDescription": "Cell only",
                  "comment": "John and Luke always end up fighting",
                  "authorisedBy": "Unknown",
                  "whenCreated": "Unknown",
                  "whenUpdated": "Unknown",
                  "updatedBy": "Unknown",
                  "isClosed": false,
                  "closedBy": null,
                  "closedReason": null,
                  "closedAt": null,
                  "otherPrisonerDetails": {
                    "prisonerNumber": "D5678EF",
                    "role": "PERPETRATOR",
                    "roleDescription": "Perpetrator",
                    "firstName": "Joseph",
                    "lastName": "Bloggs",
                    "prisonId": "MDI",
                    "prisonName": "Moorland (HMP & YOI)",
                    "cellLocation": "B-2-007"
                  },
                  "isOpen": true
                }
              ]
            }
          }

        """.removeWhitespaceAndNewlines(),
      )
    }

    it("throws bad request error if includeOpen or includeClosed not provided") {
      val errors = listOf(UpstreamApiError(type = UpstreamApiError.Type.BAD_REQUEST, causedBy = UpstreamApi.NON_ASSOCIATIONS, description = "includeOpen or includeClosed must be provided."))
      whenever(getPrisonersNonAssociationsService.execute(hmppsId, prisonId, includeOpen = "false", includeClosed = "false", filters = null)).thenReturn(
        Response(
          data = null,
          errors,
        ),
      )
      val result = mockMvc.performAuthorised("$nonAssociationsPath?includeOpen=false&includeClosed=false")
      result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
    }

    it("returns 404 not found if person not found with that specific hmpps id") {
      val errors = listOf(UpstreamApiError(type = UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.NON_ASSOCIATIONS, description = "not found."))
      whenever(getPrisonersNonAssociationsService.execute(hmppsId, prisonId, includeOpen = "true", includeClosed = "false", filters = null)).thenReturn(
        Response(
          data = null,
          errors,
        ),
      )

      val result = mockMvc.performAuthorised("$nonAssociationsPath?includeOpen=true&includeClosed=false")
      result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
    }

    it("returns 404 not found if person not found in prison") {
      val errors = listOf(UpstreamApiError(type = UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.NON_ASSOCIATIONS, description = "No prison associated with prisoner"))
      val wrongPrisonId = "MDI"
      whenever(getPrisonersNonAssociationsService.execute(hmppsId, wrongPrisonId, includeOpen = "true", includeClosed = "false", filters = ConsumerFilters(listOf("XYZ")))).thenReturn(
        Response(
          data = null,
          errors,
        ),
      )

      val result = mockMvc.performAuthorisedWithCN("/v1/prison/$wrongPrisonId/prisoners/$hmppsId/non-associations?includeOpen=true&includeClosed=false", "limited-prisons")
      result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
    }
  })
