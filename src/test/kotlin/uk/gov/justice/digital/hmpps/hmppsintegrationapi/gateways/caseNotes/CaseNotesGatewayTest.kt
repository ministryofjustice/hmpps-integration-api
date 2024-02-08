package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.caseNotes

import io.kotest.core.spec.style.DescribeSpec
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CaseNotesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.CaseNotesApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer

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
  },
)
