package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiEvent
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiEvents
import java.time.LocalDateTime

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPrisonEventsService::class],
)
internal class GetPrisonEventsServiceTest(
  private val getPrisonEventsService: GetPrisonEventsService,
  @MockitoBean val prisonApiGateway: PrisonApiGateway,
  @MockitoBean val getPersonService: GetPersonService,
) : DescribeSpec(
    {
      val prisonId = "ABC"
      val nomisId = "A1417AE"
      val fromDateTime = LocalDateTime.now()
      val requestContext = buildRequestContext("testUser")
      val prisonEvents =
        PrisonApiEvents(
          listOf(
            PrisonApiEvent(
              type = "IEP_CHANGED",
              id = 21,
              noms_id = "A1417AE",
              prison_id = "BMI",
              timestamp = "2016-10-21 15:55:06.284",
              eventData = "string",
            ),
          ),
        )

      beforeEach {
        Mockito.reset(prisonApiGateway)
      }

      it("will return 200 and a list of events with no hmpps id") {
        whenever(prisonApiGateway.getEvents(prisonId, null, null, fromDateTime, null, requestContext)).thenReturn(
          Response(data = prisonEvents),
        )
        val response = getPrisonEventsService.execute(prisonId, null, null, fromDateTime, null, requestContext)

        response.data.shouldNotBeNull()
        response.data.events[0]
          .type
          .shouldBe("IEP_CHANGED")
        response.data.events[0]
          .id
          .shouldBe(21)
        response.data.events[0]
          .noms_id
          .shouldBe("A1417AE")
        response.data.events[0]
          .prison_id
          .shouldBe("BMI")
        response.data.events[0]
          .timestamp
          .shouldBe("2016-10-21T14:55:06.284Z")
        response.data.events[0]
          .eventData
          .shouldBe("string")
      }

      it("will return 200 and a list of events with valid hmpps id") {
        whenever(prisonApiGateway.getEvents(prisonId, nomisId, null, fromDateTime, null, requestContext)).thenReturn(
          Response(data = prisonEvents),
        )
        whenever(getPersonService.getNomisNumber(nomisId)).thenReturn(
          Response(data = NomisNumber("A1417AE")),
        )
        val response = getPrisonEventsService.execute(prisonId, nomisId, null, fromDateTime, null, requestContext)

        response.data.shouldNotBeNull()
        response.data.events[0]
          .type
          .shouldBe("IEP_CHANGED")
        response.data.events[0]
          .id
          .shouldBe(21)
        response.data.events[0]
          .noms_id
          .shouldBe("A1417AE")
        response.data.events[0]
          .prison_id
          .shouldBe("BMI")
        response.data.events[0]
          .timestamp
          .shouldBe("2016-10-21T14:55:06.284Z")
        response.data.events[0]
          .eventData
          .shouldBe("string")
      }

      it("will return 404 with invalid hmpps id") {
        whenever(prisonApiGateway.getEvents(prisonId, nomisId, null, fromDateTime, null, requestContext)).thenReturn(
          Response(data = prisonEvents),
        )
        whenever(getPersonService.getNomisNumber(nomisId)).thenReturn(
          Response(data = null),
        )
        val response = getPrisonEventsService.execute(prisonId, nomisId, null, fromDateTime, null, requestContext)

        response.data.shouldBeNull()
        response.errors.shouldHaveSize(1)
        response.errors[0].type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
        response.errors[0].causedBy.shouldBe(UpstreamApi.PRISON_API)
      }
    },
  )
