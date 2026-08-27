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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.MovementItem
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiMovements

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetMovementService::class],
)
class GetMovementsForPersonServiceTest(
  @MockitoBean val prisonApiGateway: PrisonApiGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getMovementService: GetMovementService,
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
        whenever(prisonApiGateway.getMovementsForPerson(nomisNumber, requestContext)).thenReturn(
          Response(
            PrisonApiMovements(
              listOf(
                MovementItem(
                  "Millsike (HMP) TRN OUT",
                  "Moorland (HMP & YOI) TRN OUT",
                  "TRN",
                  "OUT",
                  "2026-07-14",
                  "12:41:05",
                  "Normal Transfer TRN OUT",
                  "NOTR TRN OUT",
                ),
                MovementItem(
                  "Millsike (HMP) CRT OUT",
                  "Moorland (HMP & YOI) CRT OUT",
                  "CRT",
                  "OUT",
                  "2026-08-14",
                  "12:41:05",
                  "Normal Transfer CRT OUT",
                  "NOTR CRT OUT",
                ),
              ),
            ),
          ),
        )
      }

      it("performs a search according to hmpps Id") {
        getMovementService.getMovement(hmppsId, requestContext)
        verify(getPersonService, times(1)).getNomisNumber(hmppsId, filters)
      }

      it("should return movements from gateway") {
        val result = getMovementService.getMovement(hmppsId, requestContext)
        result.data
          .shouldNotBeNull()
        result.data.transferReason.shouldBe("Normal Transfer TRN OUT")
        result.data.movementCode.shouldBe("NOTR TRN OUT")
        result.data.toAgencyDescription.shouldBe("Moorland (HMP & YOI) TRN OUT")
        result.data.receivedFromDescription.shouldBe("Millsike (HMP) TRN OUT")
        result.data.establishmentName.shouldBe("Millsike (HMP) TRN OUT")
        result.data.movementDateTime.shouldBe("2026-07-14T11:41:05Z")
        result.data.courtName.shouldBe("Moorland (HMP & YOI) CRT OUT")
        result.data.dateOfFirstMovement.shouldBe("2026-07-14T11:41:05Z")
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

        val result = getMovementService.getMovement(hmppsId, requestContext)
        result.errors.shouldBe(errors)
      }

      it("return entity not found if getPersonService returns no nomis number") {
        whenever(getPersonService.getNomisNumber(hmppsId, filters)).thenReturn(
          Response(
            data = NomisNumber(null),
          ),
        )

        val result = getMovementService.getMovement(hmppsId, requestContext)
        result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)))
      }

      it("return errors if movements gateway returns an error") {
        val errors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
              causedBy = UpstreamApi.PRISON_API,
              description = "Mock error from movements gateway",
            ),
          )
        whenever(prisonApiGateway.getMovementsForPerson(nomisNumber, requestContext)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = getMovementService.getMovement(hmppsId, requestContext)
        result.errors.shouldBe(errors)
      }
    },
  )
