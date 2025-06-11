package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.activities

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
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
class GetScheduledEventsGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val activitiesGateway: ActivitiesGateway,
) : DescribeSpec(
    {
      val objectMapper = jacksonObjectMapper()
      val prisonCode = "MDI"
      val date = "2022-11-01"
      val path = "/scheduled-events/prison/$prisonCode?date=$date"
      val activitiesApiMockServer = ApiMockServer.create(UpstreamApi.ACTIVITIES)
      val prisonerNumbers = listOf("A8451DY", "A8452DY", "A8650DY", "A8633DY")
      val requestBodyMap = mapOf("prisonerNumbers" to prisonerNumbers)

      val jsonRequest = objectMapper.writeValueAsString(requestBodyMap)

      beforeEach {
        activitiesApiMockServer.start()

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("ACTIVITIES")).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )
      }

      afterTest {
        activitiesApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        activitiesGateway.getScheduledEvents(prisonCode, prisonerNumbers, date, null)

        verify(hmppsAuthGateway, times(1))
          .getClientToken("ACTIVITIES")
      }

      it("upstream API returns a bad request error, throw exception") {
        activitiesApiMockServer.stubForPost(path, jsonRequest, "", HttpStatus.BAD_REQUEST)
        val result = activitiesGateway.getScheduledEvents(prisonCode, prisonerNumbers, date, null)
        result.errors.shouldBe(listOf(UpstreamApiError(causedBy = UpstreamApi.ACTIVITIES, type = UpstreamApiError.Type.BAD_REQUEST)))
      }

      it("upstream API returns an forbidden error, throw forbidden exception") {
        activitiesApiMockServer.stubForPost(path, jsonRequest, "", HttpStatus.FORBIDDEN)
        val response = activitiesGateway.getScheduledEvents(prisonCode, prisonerNumbers, date, null)
        response.errors.shouldHaveSize(1)
        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.FORBIDDEN)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.ACTIVITIES)
      }

      it("returns scheduled events") {
        activitiesApiMockServer.stubForPost(path, jsonRequest, File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/activities/fixtures/GetScheduledEvents.json").readText(), HttpStatus.OK)
        val response = activitiesGateway.getScheduledEvents(prisonCode, prisonerNumbers, date, null)
        println("Response: $response")
        response.data!!.prisonCode.shouldBe(prisonCode)
      }
    },
  )
