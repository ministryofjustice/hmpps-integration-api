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
          lastPrisonId = knownPrisonId,
          lastMovementType = LastMovementType.ADMISSION,
          receptionDate = LocalDate.of(2025, 9, 30),
        )

      val prisonerBaseLocationReleased =
        PrisonerBaseLocation(
          inPrison = false,
          lastPrisonId = releasedPrisonId,
          lastMovementType = LastMovementType.ADMISSION,
          receptionDate = LocalDate.of(2025, 9, 30),
        )

      val prisonerBaseLocationInvalidPrison =
        PrisonerBaseLocation(
          inPrison = true,
          prisonId = anotherPrisonId,
          lastPrisonId = anotherPrisonId,
          lastMovementType = LastMovementType.ADMISSION,
          receptionDate = LocalDate.of(2025, 9, 30),
        )

      val knownHmppsId = "A123456"
      val anotherHmppsId = "B222222"
      val unknownHmppsId = "Z999999"
      val hmppsId = knownHmppsId

      val filters = ConsumerFilters(null)

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

        lenient().whenever(getPersonService.getNomisNumberWithPrisonFilter(knownHmppsId, filters)).thenReturn(Response(data = NomisNumber(knownNomisNumber)))
        lenient().whenever(getPersonService.getNomisNumberWithPrisonFilter(anotherHmppsId, filters)).thenReturn(Response(data = NomisNumber(anotherNomisNumber)))
        lenient().whenever(getPersonService.getNomisNumberWithPrisonFilter(unknownHmppsId, filters)).thenReturn(prisonerNotFoundErrorResponse())

        lenient().whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<PrisonerBaseLocation>(knownPrisonId, filters)).thenReturn(Response(data = null, errors = emptyList()))
        lenient().whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<PrisonerBaseLocation>(releasedPrisonId, filters)).thenReturn(Response(data = null, errors = emptyList()))
        lenient().whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<PrisonerBaseLocation>(anotherPrisonId, filters)).thenReturn(prisonAccessDeniedResponse())
      }

      it("calls getNomisNumberWithPrisonFilter") {
        givenLocationIsFound(knownNomisNumber, prisonerBaseLocationReceived)
        getPrisonerBaseLocationForPersonService.execute(hmppsId, filters)
        verify(getPersonService, times(1)).getNomisNumberWithPrisonFilter(hmppsId, filters)
      }

      it("returns prisoner base location") {
        givenLocationIsFound(knownNomisNumber, prisonerBaseLocationReceived)
        val expectedLocation = prisonerBaseLocationReceived.copy()

        val response = getPrisonerBaseLocationForPersonService.execute(hmppsId, filters)
        println("response = $response")
        response.data shouldBe expectedLocation
      }

      it("returns the upstream error when an error occurs") {
        val errorResponse = prisonerNotFoundErrorResponse()
        whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(errorResponse)

        val response = getPrisonerBaseLocationForPersonService.execute(hmppsId, filters)
        response.errors shouldBe errorResponse.errors
      }

      it("does not return prisoner base location of known person, when location not found") {
        val errorResponse = prisonerBaseLocationNotFoundErrorResponse()
        givenLocationIsNotFound(knownNomisNumber)

        val response = getPrisonerBaseLocationForPersonService.execute(knownHmppsId, filters)
        response.errors shouldBe errorResponse.errors
      }

      it("failed to get prisoners nomis number of unknown prisoners") {
        val errorResponse = nomisNumberNotFoundNDeliusErrorResponse()
        whenever(getPersonService.getNomisNumberWithPrisonFilter(unknownHmppsId, filters)).thenReturn(errorResponse)

        val response = getPrisonerBaseLocationForPersonService.execute(unknownHmppsId, filters)
        response.errors shouldBe errorResponse.errors
      }

      it("failed to get prisoners nomis number of known person") {
        val errorResponse = nomisNumberMissingResponse()
        whenever(getPersonService.getNomisNumberWithPrisonFilter(anotherHmppsId, filters)).thenReturn(Response(data = NomisNumber(null)))

        val response = getPrisonerBaseLocationForPersonService.execute(anotherHmppsId, filters)
        response.errors shouldBe errorResponse.errors
      }

      it("returns location when last known prison ID is OUT") {
        givenLocationIsFound(knownNomisNumber, prisonerBaseLocationReleased)
        val expectedLocation = prisonerBaseLocationReleased.copy()

        val response = getPrisonerBaseLocationForPersonService.execute(hmppsId, filters)
        println("response = $response")
        response.data shouldBe expectedLocation
      }

      it("returns error when last known prison ID is another prisoner ID - not in filters") {
        givenLocationIsFound(knownNomisNumber, prisonerBaseLocationInvalidPrison)
        val errorresponse = prisonAccessDeniedResponse()

        val response = getPrisonerBaseLocationForPersonService.execute(hmppsId, filters)
        println("response = $response")
        response.errors shouldBe errorresponse.errors
      }
    },
  )
