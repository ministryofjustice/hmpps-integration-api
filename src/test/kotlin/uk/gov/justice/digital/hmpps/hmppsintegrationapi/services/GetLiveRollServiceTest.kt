package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiLiveRoll

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetLiveRollService::class],
)
internal class GetLiveRollServiceTest(
  private val getLiveRollService: GetLiveRollService,
  @MockitoBean val prisonApiGateway: PrisonApiGateway,
) : DescribeSpec({
    val prisonId = "ABC"
    val requestContext = buildRequestContext("testUser")
    val liveRoll = PrisonApiLiveRoll(listOf("A5155DY", "A9971DY"))

    beforeEach {
      Mockito.reset(prisonApiGateway)
    }

    it("will return 200 and a list of prisoners ids") {
      whenever(
        prisonApiGateway.getLiveRoll(
          prisonId,
          requestContext = requestContext,
        ),
      ).thenReturn(
        Response(data = liveRoll),
      )
      val response =
        getLiveRollService.execute(
          prisonId,
          requestContext = requestContext,
        )
      response.data.shouldNotBeNull()
      response.data.noms_ids[0].shouldBe("A5155DY")
      response.data.noms_ids[1].shouldBe("A9971DY")
    }

    it("will return 404 with no prisoner ids") {
      whenever(
        prisonApiGateway.getLiveRoll(
          "123",
          requestContext = requestContext,
        ),
      ).thenReturn(
        Response(
          data = PrisonApiLiveRoll(listOf()),
        ),
      )
      try {
        getLiveRollService.execute(
          "123",
          requestContext = requestContext,
        )
      } catch (e: Exception) {
        e.shouldNotBeNull()
        e.message.shouldBe("Prison ID 123 did not return any persons")
      }
    }
  })
