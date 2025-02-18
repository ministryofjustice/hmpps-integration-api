package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.prisonVisits

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonVisitsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.PrisonVisitsApiMockServer

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonVisitsGateway::class],
)
class PrisonVisitsGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val prisonVisitsGateway: PrisonVisitsGateway,
) : DescribeSpec(
    {
      val visitReference: String = "123456"

      val prisonVisitsApiMockServer = PrisonVisitsApiMockServer()

      beforeEach {
        prisonVisitsApiMockServer.start()
        Mockito.reset(hmppsAuthGateway)

        whenever(hmppsAuthGateway.getClientToken("MANAGE-PRISON-VISITS")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        prisonVisitsApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials for linked prisoners api") {
        prisonVisitsGateway.getVisitByReference(visitReference)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("MANAGE-PRISON-VISITS")
      }

      it("returns a 500 when request is incorrect") {
        val path = "/visits/$visitReference"
        prisonVisitsApiMockServer.stubPrisonVisitsApiResponse(path, body = "", HttpStatus.INTERNAL_SERVER_ERROR)

        val response = prisonVisitsGateway.getVisitByReference(visitReference)
        response.errors.shouldHaveSize(1)
        response.errors.shouldContain(HttpStatus.INTERNAL_SERVER_ERROR)
      }
    },
  )
