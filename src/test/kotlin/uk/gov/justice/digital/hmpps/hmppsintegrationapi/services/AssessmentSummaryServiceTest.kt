package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AssessRisksAndNeedsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.time.LocalDateTime

class AssessmentSummaryServiceTest {
  lateinit var assessmentSummaryService: AssessmentSummaryService
  val assessRisksAndNeedsGateway = mock<AssessRisksAndNeedsGateway>()
  val personService = mock<GetPersonService>()
  val crn = "X123456"
  val assessmentSummary =
    AssessmentSummary(
      initiationDate = LocalDateTime.of(2026, 1, 22, 1, 2, 3),
      completedDate = LocalDateTime.of(2026, 1, 22, 1, 2, 3),
      assessmentType = "Test Assessment Type",
      status = "Test Assessment Status",
      assessorName = "Test Assessor Name",
      countersignerName = "Test Countersigner Name",
    )

  @BeforeEach
  fun setup() {
    whenever(personService.execute(crn)).thenReturn(
      Response(
        data =
          Person(
            firstName = "TestFirstName",
            lastName = "TestSurname",
            identifiers = Identifiers(deliusCrn = crn),
          ),
      ),
    )
    whenever(assessRisksAndNeedsGateway.getAssessmentSummary(crn)).thenReturn(Response(assessmentSummary))
    assessmentSummaryService = AssessmentSummaryService(assessRisksAndNeedsGateway, personService)
  }

  @Test
  fun `returns an assessment summary`() {
    val response = assessmentSummaryService.assessmentSummary(crn)
    response.data.shouldBe(assessmentSummary)
  }

  @Test
  fun `returns an error if person service returns an error`() {
    val errors =
      listOf(
        UpstreamApiError(
          causedBy = UpstreamApi.NDELIUS,
          type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
        ),
      )
    whenever(personService.execute(crn)).thenReturn(Response(null, errors))
    val response = assessmentSummaryService.assessmentSummary(crn)
    response.errors.shouldBe(errors)
  }

  @Test
  fun `returns an error if getAssessmentSummary returns an error`() {
    val errors =
      listOf(
        UpstreamApiError(
          causedBy = UpstreamApi.ASSESS_RISKS_AND_NEEDS,
          type = UpstreamApiError.Type.FORBIDDEN,
        ),
      )
    whenever(assessRisksAndNeedsGateway.getAssessmentSummary(crn)).thenReturn(Response(null, errors))
    val response = assessmentSummaryService.assessmentSummary(crn)
    response.errors.shouldBe(errors)
  }
}
