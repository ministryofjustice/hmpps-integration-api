package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.kotest.core.spec.style.DescribeSpec
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.Visit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.VisitContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.VisitorSupport
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetVisitInformationByReferenceService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [VisitsController::class])
@ActiveProfiles("test")
class VisitsControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getVisitInformationByReferenceService: GetVisitInformationByReferenceService,
) : DescribeSpec(
    {
      val visitReference = "1234567"
      val path = "/v1/visits/$visitReference"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)
      val visitResponse = Visit(prisonerId = "PrisonerId", prisonId = "MDI", prisonName = "Some Prison", visitRoom = "Room", visitType = "Type", visitStatus = "Status", outcomeStatus = "Outcome", visitRestriction = "Restriction", startTimestamp = "Start", endTimestamp = "End", createdTimestamp = "Created", modifiedTimestamp = "Modified", firstBookedDateTime = "First", visitors = emptyList(), visitNotes = emptyList(), visitContact = VisitContact(name = "Name", telephone = "Telephone", email = "Email"), visitorSupport = VisitorSupport(description = "Description"))

      it("calls the visit information service and successfully retrieves the visit information") {
        mockMvc.performAuthorised(path)

        verify(getVisitInformationByReferenceService, VerificationModeFactory.times(1)).execute(visitReference, filters = null)
      }
    },
  )
