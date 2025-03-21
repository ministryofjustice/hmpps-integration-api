package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.HmppsIdConverter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactRestriction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonVisitRestriction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerContactRestrictions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetVisitRestrictionsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetVisitorRestrictionsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [VisitRestrictionsController::class])
@ActiveProfiles("test")
internal class VisitRestrictionsControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getVisitRestrictionsForPersonService: GetVisitRestrictionsForPersonService,
  @MockitoBean val getVisitorRestrictionsService: GetVisitorRestrictionsService,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val hmppsIdConverter: HmppsIdConverter,
  @MockitoBean val getCaseAccess: GetCaseAccess,
) : DescribeSpec(
    {
      val hmppsId = "A1234AA"
      val contactId = 123456L
      val visitRestrictionsPath = "/v1/persons/$hmppsId/visit-restrictions"
      val restrictionsPath = "/v1/persons/$hmppsId/visitor/$contactId/restrictions"
      val prisonerContactRestrictionsResponse =
        mutableListOf(
          ContactRestriction(
            restrictionType = "BAN",
            restrictionTypeDescription = "Banned",
            startDate = "2024-01-01",
            expiryDate = "2024-01-01",
            comments = "N/A",
            enteredByUsername = "user123",
            enteredByDisplayName = "User Name",
            createdBy = "admin",
            createdTime = "2024-01-01T12:00:00Z",
            updatedBy = "admin",
            updatedTime = "2024-01-02T12:00:00Z",
          ),
        )

      val contactGlobalRestrictionsResponse =
        listOf(
          ContactRestriction(
            restrictionType = "BAN",
            restrictionTypeDescription = "Banned",
            startDate = "2024-01-01",
            expiryDate = "2024-01-01",
            comments = "N/A",
            enteredByUsername = "user123",
            enteredByDisplayName = "User Name",
            createdBy = "admin",
            createdTime = "2024-01-01T12:00:00Z",
            updatedBy = "admin",
            updatedTime = "2024-01-02T12:00:00Z",
          ),
        )

      val prisonerContactRestrictions = PrisonerContactRestrictions(prisonerContactRestrictionsResponse, contactGlobalRestrictionsResponse)

      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      beforeTest {
        Mockito.reset(getVisitRestrictionsForPersonService)
        Mockito.reset(getVisitorRestrictionsService)
        Mockito.reset(auditService)
        whenever(getVisitRestrictionsForPersonService.execute(hmppsId, filters = null)).thenReturn(
          Response(
            data =
              listOf(
                PersonVisitRestriction(restrictionId = 1, comment = "Restriction 1", restrictionType = "TYPE", restrictionTypeDescription = "Type description", startDate = "2025-01-01", expiryDate = "2025-12-31", active = true),
                PersonVisitRestriction(restrictionId = 2, comment = "Restriction 2", restrictionType = "TYPE", restrictionTypeDescription = "Type description", startDate = "2025-01-01", expiryDate = "2025-12-31", active = true),
              ),
          ),
        )

        whenever(getVisitorRestrictionsService.execute(hmppsId, contactId, filters = null)).thenReturn(
          Response(
            data = prisonerContactRestrictions,
          ),
        )
      }

      it("returns 200 when successfully find visit restrictions") {
        val result = mockMvc.performAuthorised(visitRestrictionsPath)
        result.response.status.shouldBe(HttpStatus.OK.value())
        result.response.contentAsString.shouldBe(
          """
          {
            "data": [
              {
                "restrictionId" : 1,
                "comment" : "Restriction 1",
                "restrictionType": "TYPE",
                "restrictionTypeDescription" : "Type description",
                "startDate" : "2025-01-01",
                "expiryDate" : "2025-12-31",
                "active" : true
              },
              {
                "restrictionId" : 2,
                "comment" : "Restriction 2",
                "restrictionType": "TYPE",
                "restrictionTypeDescription" : "Type description",
                "startDate" : "2025-01-01",
                "expiryDate" : "2025-12-31",
                "active" : true
              }
          ]
        }
        """.removeWhitespaceAndNewlines(),
        )
      }

      it("returned 404 when service returns entity not found") {
        whenever(getVisitRestrictionsForPersonService.execute(hmppsId)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  causedBy = UpstreamApi.NOMIS,
                ),
              ),
          ),
        )
        val result = mockMvc.performAuthorised(visitRestrictionsPath)
        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }

      it("returned 400 when service returns bad request") {
        whenever(getVisitRestrictionsForPersonService.execute(hmppsId)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.BAD_REQUEST,
                  causedBy = UpstreamApi.NOMIS,
                ),
              ),
          ),
        )
        val result = mockMvc.performAuthorised(visitRestrictionsPath)
        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      }

      it("logs audit for visit restrictions") {
        mockMvc.performAuthorised(visitRestrictionsPath)

        verify(
          auditService,
          VerificationModeFactory.times(1),
        ).createEvent("GET_PERSON_VISIT_RESTRICTIONS", mapOf("hmppsId" to hmppsId))
      }

      // restrictions endpoint
      it("returns 200 when visitor relationships are successfully found") {
        val result = mockMvc.performAuthorised(restrictionsPath)
        result.response.status.shouldBe(HttpStatus.OK.value())
        result.response.contentAsString.shouldBe(
          """
        {
          "data":
            {
              "prisonerContactRestrictions": [
                {
                  "restrictionType": "BAN",
                  "restrictionTypeDescription": "Banned",
                  "startDate": "2024-01-01",
                  "expiryDate": "2024-01-01",
                  "comments": "N/A",
                  "enteredByUsername": "user123",
                  "enteredByDisplayName": "User Name",
                  "createdBy": "admin",
                  "createdTime": "2024-01-01T12:00:00Z",
                  "updatedBy": "admin",
                  "updatedTime": "2024-01-02T12:00:00Z"
                }
              ],
              "contactGlobalRestrictions": [
                {
                  "restrictionType": "BAN",
                  "restrictionTypeDescription": "Banned",
                  "startDate": "2024-01-01",
                  "expiryDate": "2024-01-01",
                  "comments": "N/A",
                  "enteredByUsername": "user123",
                  "enteredByDisplayName": "User Name",
                  "createdBy": "admin",
                  "createdTime": "2024-01-01T12:00:00Z",
                  "updatedBy": "admin",
                  "updatedTime": "2024-01-02T12:00:00Z"
                }
              ]
            }
        }
        """.removeWhitespaceAndNewlines(),
        )
      }

      it("returned 404 when GetVisitorRestrictionsService returns entity not found") {
        whenever(getVisitorRestrictionsService.execute(hmppsId, contactId, filters = null)).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  causedBy = UpstreamApi.PERSONAL_RELATIONSHIPS,
                ),
              ),
          ),
        )
        val result = mockMvc.performAuthorised(restrictionsPath)
        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }

      it("returned 404 when consumerPrisonAccessService returns entity not found") {
        whenever(getVisitorRestrictionsService.execute(hmppsId, contactId, filters = ConsumerFilters(listOf("XYZ")))).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  causedBy = UpstreamApi.NOMIS,
                ),
              ),
          ),
        )

        val result = mockMvc.performAuthorisedWithCN(restrictionsPath, "limited-prisons")
        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }

      it("returned 400 when GetVisitorRestrictionsService returns bad request") {
        whenever(getVisitorRestrictionsService.execute(hmppsId, contactId, filters = null)).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.BAD_REQUEST,
                  causedBy = UpstreamApi.PERSONAL_RELATIONSHIPS,
                ),
              ),
          ),
        )
        val result = mockMvc.performAuthorised(restrictionsPath)
        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      }

      it("logs audit for visit relationships") {
        mockMvc.performAuthorised(restrictionsPath)

        verify(
          auditService,
          VerificationModeFactory.times(1),
        ).createEvent("GET_VISITOR_RESTRICTIONS", mapOf("hmppsId" to hmppsId, "contactId" to contactId.toString()))
      }
    },
  )
