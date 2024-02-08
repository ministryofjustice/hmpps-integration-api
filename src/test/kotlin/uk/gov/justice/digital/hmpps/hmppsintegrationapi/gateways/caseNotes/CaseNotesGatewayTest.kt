package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.caseNotes

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CaseNotesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.CaseNotesApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PageCaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [CaseNotesGateway::class],
)
class CaseNotesGatewayTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  val caseNotesGateway: CaseNotesGateway,
) : DescribeSpec(
    {
      val caseNotesApiMockServer = CaseNotesApiMockServer()
      beforeEach {
        caseNotesApiMockServer.start()

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("CaseNotes")).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )
      }

      afterTest {
        caseNotesApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        caseNotesGateway.getCaseNotesForPerson(id = "123")

        org.mockito.kotlin.verify(hmppsAuthGateway, org.mockito.internal.verification.VerificationModeFactory.times(1))
          .getClientToken("CaseNotes")
      }

      it("upstream API returns an error, return error") {
        caseNotesApiMockServer.stubGetCaseNotes("123", "", HttpStatus.BAD_REQUEST)
        val response = caseNotesGateway.getCaseNotesForPerson(id = "123")
        response.data.shouldBe(PageCaseNote(null))
        response.errors[0].type.shouldBe(UpstreamApiError.Type.BAD_REQUEST)
      }
    },
  )
