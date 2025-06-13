package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.activities

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesPrisonPayBand
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ActivitiesGateway::class],
)
class GetPrisonPayBandsGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val activitiesGateway: ActivitiesGateway,
) : DescribeSpec({
    val mockServer = ApiMockServer.create(UpstreamApi.ACTIVITIES)
    val prisonCode = "MDI"

    beforeEach {
      mockServer.start()

      Mockito.reset(hmppsAuthGateway)
      whenever(hmppsAuthGateway.getClientToken("ACTIVITIES")).thenReturn(
        HmppsAuthMockServer.TOKEN,
      )
    }

    afterEach {
      mockServer.stop()
    }

    it("authenticates using HMPPS Auth with credentials") {
      activitiesGateway.getPrisonPayBands(prisonCode)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("ACTIVITIES")
    }

    it("Returns a prison regime") {
      mockServer.stubForGet(
        "/prison/$prisonCode/prison-pay-bands",
        File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/activities/fixtures/GetPrisonPayBands.json").readText(),
      )

      val result = activitiesGateway.getPrisonPayBands(prisonCode)
      result.errors.shouldBeEmpty()
      result.data.shouldNotBeNull()
      result.data.size.shouldBe(1)
      result.data[0].shouldBe(
        ActivitiesPrisonPayBand(
          id = 123456,
          displaySequence = 1,
          alias = "Low",
          description = "Pay band 1",
          nomisPayBand = 1,
          prisonCode = "MDI",
          createdTime = "2025-06-13T14:25:19.385Z",
          createdBy = "string",
          updatedTime = "2025-06-13T14:25:19.385Z",
          updatedBy = "string",
        ),
      )
    }

    it("Returns a bad request error") {
      mockServer.stubForGet(
        "/prison/$prisonCode/prison-pay-bands",
        "{}",
        HttpStatus.BAD_REQUEST,
      )

      val result = activitiesGateway.getPrisonPayBands(prisonCode)
      result.errors.shouldBe(listOf(UpstreamApiError(causedBy = UpstreamApi.ACTIVITIES, type = UpstreamApiError.Type.BAD_REQUEST)))
    }
  })
