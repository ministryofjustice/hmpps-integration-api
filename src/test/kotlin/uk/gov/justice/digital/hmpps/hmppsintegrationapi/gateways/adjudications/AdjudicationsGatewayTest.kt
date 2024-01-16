package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.adjudications

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AdjudicationsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.AdjudicationsApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [AdjudicationsGateway::class],
)
class AdjudicationsGatewayTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  val adjudicationsGateway: AdjudicationsGateway,
) : DescribeSpec(
  {
    val adjudicationsApiMockServer = AdjudicationsApiMockServer()
    beforeEach {
      adjudicationsApiMockServer.start()

      Mockito.reset(hmppsAuthGateway)
      whenever(hmppsAuthGateway.getClientToken("Adjudications")).thenReturn(
        HmppsAuthMockServer.TOKEN,
      )
    }
    afterTest {
      adjudicationsApiMockServer.stop()
    }

    it("authenticates using HMPPS Auth with credentials") {
      adjudicationsGateway.getReportedAdjudicationsForPerson(id = "123")

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("Adjudications")
    }

    it("upstream API returns an error, return error") {
      adjudicationsApiMockServer.stubGetReportedAdjudicationsForPerson("123", "", HttpStatus.BAD_REQUEST)
      val response = adjudicationsGateway.getReportedAdjudicationsForPerson(id = "123")
      response.data.shouldBe(emptyList())
      response.errors[0].type.shouldBe(UpstreamApiError.Type.BAD_REQUEST)
    }

    it("returns AadjudicationResponse") {
      adjudicationsApiMockServer.stubGetReportedAdjudicationsForPerson(
        "123",
        "[\n" +
          "  {\n" +
          "    \"chargeNumber\": \"string\",\n" +
          "    \"prisonerNumber\": \"G2996UX\",\n" +
          "    \"gender\": \"MALE\",\n" +
          "    \"incidentDetails\": {\n" +
          "      \"locationId\": 0,\n" +
          "      \"dateTimeOfIncident\": \"2021-07-05T10:35:17\",\n" +
          "      \"dateTimeOfDiscovery\": \"2021-07-05T10:35:17\",\n" +
          "      \"handoverDeadline\": \"2021-07-05T10:35:17\"\n" +
          "    }" +
          "}]",
        HttpStatus.OK,
      )
      val response = adjudicationsGateway.getReportedAdjudicationsForPerson(id = "123")
      response.data.count().shouldBe(1)
      response.data.first().incidentDetails.dateTimeOfIncident.shouldBe("2021-07-05T10:35:17")
    }
  },
)
