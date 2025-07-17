package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.activities

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesEarliestReleaseDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesWaitingListApplication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ActivitiesGateway::class],
)
class GetWaitingListApplicationsByScheduleIdGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val activitiesGateway: ActivitiesGateway,
) : DescribeSpec(
    {
      val mockServer = ApiMockServer.create(UpstreamApi.ACTIVITIES)
      val scheduleId = 123456L
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
        mockServer.resetValidator()
      }

      it("authenticates using HMPPS Auth with credentials") {
        activitiesGateway.getWaitingListApplicationsByScheduleId(scheduleId, prisonCode)

        verify(hmppsAuthGateway, times(1)).getClientToken("ACTIVITIES")
      }

      it("Returns a waiting list application") {
        val path = "/integration-api/schedules/$scheduleId/waiting-list-applications"
        mockServer.stubForGet(path, File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/activities/fixtures/GetWaitingListApplicationsByScheduleId.json").readText(), HttpStatus.OK)

        val result = activitiesGateway.getWaitingListApplicationsByScheduleId(scheduleId, prisonCode)
        result.errors.shouldBeEmpty()
        result.data.shouldNotBeNull()
        result.data.shouldBe(
          listOf(
            ActivitiesWaitingListApplication(
              id = 111111L,
              activityId = 1000,
              scheduleId = 222222,
              allocationId = 333333,
              prisonCode = "MDI",
              prisonerNumber = "G2996UX",
              bookingId = 10001,
              status = "PENDING",
              statusUpdatedTime = LocalDateTime.parse("2023-06-04T16:30:00"),
              requestedDate = LocalDate.parse("2023-06-23"),
              requestedBy = "Fred Bloggs",
              comments = "The prisoner has specifically requested to attend this activity",
              declinedReason = "The prisoner has specifically requested to attend this activity",
              creationTime = LocalDateTime.parse("2023-01-03T12:00:00"),
              createdBy = "Jon Doe",
              updatedTime = LocalDateTime.parse("2023-01-04T16:30:00"),
              updatedBy = "Jane Doe",
              earliestReleaseDate =
                ActivitiesEarliestReleaseDate(
                  releaseDate = "2027-09-20",
                  isTariffDate = true,
                  isIndeterminateSentence = true,
                  isImmigrationDetainee = true,
                  isConvictedUnsentenced = true,
                  isRemand = true,
                ),
              nonAssociations = true,
            ),
          ),
        )

        mockServer.verify(
          getRequestedFor(urlEqualTo(path))
            .withHeader("Caseload-Id", equalTo(prisonCode)),
        )

        mockServer.assertValidationPassed()
      }

      it("Returns a bad request error") {
        mockServer.stubForGet("/integration-api/schedules/$scheduleId/waiting-list-applications", File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/activities/fixtures/GetWaitingListApplicationsByScheduleId.json").readText(), HttpStatus.BAD_REQUEST)

        val result = activitiesGateway.getWaitingListApplicationsByScheduleId(scheduleId, prisonCode)
        result.errors.shouldBe(listOf(UpstreamApiError(causedBy = UpstreamApi.ACTIVITIES, type = UpstreamApiError.Type.BAD_REQUEST)))
      }
    },
  )
