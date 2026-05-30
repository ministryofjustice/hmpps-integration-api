package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.Mockito.lenient
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerBaseLocationGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LastMovementType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerBaseLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import java.time.LocalDate

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPrisonerBaseLocationForPersonService::class],
)
internal class GetPrisonerBaseLocationForPersonServiceTest(
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @MockitoBean val prisonerBaseLocationGateway: PrisonerBaseLocationGateway,
  private val getPrisonerBaseLocationForPersonService: GetPrisonerBaseLocationForPersonService,
) : DescribeSpec(
    {
      val knownNomisNumber = "A1234BC"
      val anotherNomisNumber = "B2222CD"

      val knownPrisonId = "MDI"
      val anotherPrisonId = "ABC"
      val releasedPrisonId = "OUT"

      val prisonerBaseLocationReceived =
        PrisonerBaseLocation(
          inPrison = true,
          prisonId = knownPrisonId,
          lastPrisonId = anotherPrisonId,
          lastMovementType = LastMovementType.ADMISSION,
          receptionDate = LocalDate.of(2025, 9, 30),
        )

      val prisonerBaseLocationReleased =
        PrisonerBaseLocation(
          inPrison = false,
          lastPrisonId = knownPrisonId,
          lastMovementType = LastMovementType.ADMISSION,
          receptionDate = LocalDate.of(2025, 9, 30),
        )

      val knownHmppsId = "A123456"
      val anotherHmppsId = "B222222"
      val unknownHmppsId = "Z999999"
      val hmppsId = knownHmppsId

      val requestContext = buildRequestContext(filters = ConsumerFilters(null))

      fun prisonerNotFoundErrorResponse(): Response<NomisNumber?> = Response(data = null, errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)))

      fun prisonerBaseLocationNotFoundErrorResponse(): Response<PrisonerBaseLocation?> = Response(data = null, errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)))

      fun nomisNumberNotFoundNDeliusErrorResponse(): Response<NomisNumber?> = Response(data = null, errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NDELIUS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)))

      fun nomisNumberMissingResponse(): Response<NomisNumber?> = Response(data = null, errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)))

      fun prisonAccessDeniedResponse(): Response<PrisonerBaseLocation?> = Response(data = null, errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)))

      fun givenLocationIsFound(
        nomisNumber: String,
        prisonerBaseLocation: PrisonerBaseLocation,
      ) = whenever(prisonerBaseLocationGateway.getPrisonerBaseLocation(nomisNumber)).thenReturn(Response(data = prisonerBaseLocation))

      fun givenLocationIsNotFound(nomisNumber: String? = null) = whenever(prisonerBaseLocationGateway.getPrisonerBaseLocation(nomisNumber ?: any())).thenReturn(prisonerBaseLocationNotFoundErrorResponse())

      beforeEach {
        Mockito.reset(getPersonService, consumerPrisonAccessService, prisonerBaseLocationGateway)

        lenient().whenever(getPersonService.getNomisNumber(knownHmppsId, requestContext)).thenReturn(Response(data = NomisNumber(knownNomisNumber)))
        lenient().whenever(getPersonService.getNomisNumber(anotherHmppsId, requestContext)).thenReturn(Response(data = NomisNumber(anotherNomisNumber)))
        lenient().whenever(getPersonService.getNomisNumber(unknownHmppsId, requestContext)).thenReturn(prisonerNotFoundErrorResponse())

        lenient().whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<PrisonerBaseLocation>(knownPrisonId, requestContext.filters)).thenReturn(Response(data = null, errors = emptyList()))
        lenient().whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<PrisonerBaseLocation>(releasedPrisonId, requestContext.filters)).thenReturn(Response(data = null, errors = emptyList()))
        lenient().whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<PrisonerBaseLocation>(anotherPrisonId, requestContext.filters)).thenReturn(prisonAccessDeniedResponse())
      }

      it("calls getNomisNumber") {
        givenLocationIsFound(knownNomisNumber, prisonerBaseLocationReceived)
        getPrisonerBaseLocationForPersonService.execute(hmppsId, requestContext)
        verify(getPersonService, times(1)).getNomisNumber(hmppsId, requestContext)
      }

      it("returns prisoner base location") {
        givenLocationIsFound(knownNomisNumber, prisonerBaseLocationReceived)
        val expectedLocation = prisonerBaseLocationReceived.copy()

        val response = getPrisonerBaseLocationForPersonService.execute(hmppsId, requestContext)
        response.data shouldBe expectedLocation
      }

      it("returns the upstream error when an error occurs") {
        val errorResponse = prisonerNotFoundErrorResponse()
        whenever(getPersonService.getNomisNumber(hmppsId, requestContext)).thenReturn(errorResponse)

        val response = getPrisonerBaseLocationForPersonService.execute(hmppsId, requestContext)
        response.errors shouldBe errorResponse.errors
      }

      it("does not return prisoner base location of known person, when location not found") {
        val errorResponse = prisonerBaseLocationNotFoundErrorResponse()
        givenLocationIsNotFound(knownNomisNumber)

        val response = getPrisonerBaseLocationForPersonService.execute(knownHmppsId, requestContext)
        response.errors shouldBe errorResponse.errors
      }

      it("failed to get prisoners nomis number of unknown prisoners") {
        val errorResponse = nomisNumberNotFoundNDeliusErrorResponse()
        whenever(getPersonService.getNomisNumber(unknownHmppsId, requestContext)).thenReturn(errorResponse)

        val response = getPrisonerBaseLocationForPersonService.execute(unknownHmppsId, requestContext)
        response.errors shouldBe errorResponse.errors
      }

      it("failed to get prisoners nomis number of known person") {
        val errorResponse = nomisNumberMissingResponse()
        whenever(getPersonService.getNomisNumber(anotherHmppsId, requestContext)).thenReturn(Response(data = NomisNumber(null)))

        val response = getPrisonerBaseLocationForPersonService.execute(anotherHmppsId, requestContext)
        response.errors shouldBe errorResponse.errors
      }

      it("returns location when last prison ID matches filter and prisoner inPrison is false") {
        whenever(prisonerBaseLocationGateway.getPrisonerBaseLocation(knownNomisNumber)).thenReturn(Response(data = prisonerBaseLocationReleased))

        getPrisonerBaseLocationForPersonService.execute(knownHmppsId, requestContext)
        verify(consumerPrisonAccessService, times(1)).checkConsumerHasPrisonAccess<Any>(prisonerBaseLocationReleased.lastPrisonId, requestContext.filters)
      }

      it("returns location when prison ID matches filter and prisoner inPrison is true") {
        whenever(prisonerBaseLocationGateway.getPrisonerBaseLocation(anotherNomisNumber)).thenReturn(Response(data = prisonerBaseLocationReceived))

        getPrisonerBaseLocationForPersonService.execute(anotherHmppsId, requestContext)
        verify(consumerPrisonAccessService, times(1)).checkConsumerHasPrisonAccess<Any>(prisonerBaseLocationReceived.prisonId, requestContext.filters)
      }
    },
  )
