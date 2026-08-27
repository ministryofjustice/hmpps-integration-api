package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiScheduledEvents

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetMovementsService::class],
)
class GetMovementsServiceTest(
  @MockitoBean val prisonApiGateway: PrisonApiGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getMovementService: GetMovementsService,
) : DescribeSpec(
    {
      val hmppsId = "1234/56789B"
      val nomisNumber = "Z99999ZZ"
      val filters = null
      val requestContext = buildRequestContext("testUser", filters = filters)

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(prisonApiGateway)

        whenever(getPersonService.getNomisNumber(hmppsId, requestContext.filters)).thenReturn(Response(NomisNumber(nomisNumber)))
        whenever(prisonApiGateway.getScheduledMovements(nomisNumber, requestContext)).thenReturn(
          Response(
            listOf(
              PrisonApiScheduledEvents(
                "PA",
                "2026-08-27T00:33:28.896Z",
                "Comment",
              ),
              PrisonApiScheduledEvents(
                "PA",
                "2026-08-27T00:33:28.896Z",
                "Comment",
              ),
            ),
          ),
        )
      }

      it("performs a search according to hmpps Id") {
        getMovementService.getMovementsSummary(hmppsId, requestContext)
        verify(getPersonService, times(1)).getNomisNumber(hmppsId, filters)
      }

      it("should return movement summary from gateway") {
        val result = getMovementService.getMovementsSummary(hmppsId, requestContext)
        result.data
          .shouldNotBeNull()
        result.data[0].diaryDateTime.shouldBe("2026-08-27T00:33:28.896Z")
        result.data[0].diaryReasonCode.shouldBe("PA")
        result.data[0].diaryComments.shouldBe("Comment")
        result.errors.count().shouldBe(0)
      }

      it("return errors if getPersonService returns an error") {
        val errors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
              causedBy = UpstreamApi.PRISON_API,
              description = "Mock error from person service",
            ),
          )
        whenever(getPersonService.getNomisNumber(hmppsId, filters)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = getMovementService.getMovementsSummary(hmppsId, requestContext)
        result.errors.shouldBe(errors)
      }

      it("return entity not found if getPersonService returns no nomis number") {
        whenever(getPersonService.getNomisNumber(hmppsId, filters)).thenReturn(
          Response(
            data = NomisNumber(null),
          ),
        )

        val result = getMovementService.getMovementsSummary(hmppsId, requestContext)
        result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)))
      }
    },
  )
