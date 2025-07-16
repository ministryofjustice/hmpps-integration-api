package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.activities

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ActivitiesGateway::class],
)
class GetAttendanceReasonsGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val activitiesGateway: ActivitiesGateway,
) : DescribeSpec(
    {
      val mockServer = ApiMockServer.create(UpstreamApi.ACTIVITIES)
      val path = "/integration-api/attendance-reasons"

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
        activitiesGateway.getAttendanceReasons()

        verify(hmppsAuthGateway, times(1)).getClientToken("ACTIVITIES")
      }

      it("Returns attendance reasons") {
        mockServer.stubForGet(
          path,
          File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/activities/fixtures/GetAttendanceReasons.json").readText(),
        )

        val result = activitiesGateway.getAttendanceReasons()
        result.errors.shouldBeEmpty()
        result.data.shouldNotBeNull()
        result.data[0].code.shouldBe("SICK")
      }

      it("upstream API returns an forbidden error, throw forbidden exception") {
        mockServer.stubForGet(path, "", HttpStatus.FORBIDDEN)
        val result = activitiesGateway.getAttendanceReasons()
        result.errors.shouldHaveSize(1)
        result.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.FORBIDDEN)
        result.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.ACTIVITIES)
      }
    },
  )
