package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.prisonerbaselocation

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.Mockito.lenient
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerBaseLocationGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LastMovementType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import java.time.LocalDate

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonerBaseLocationGateway::class],
)
class PrisonerBaseLocationGatewayTest(
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  private val prisonerBaseLocationGateway: PrisonerBaseLocationGateway,
) : DescribeSpec(
    {
      val knownNomisNumber = "A1234BC"
      val unknownNomisNumnber = "Z9876YX"

      fun prisonerNotFoundErrorResponse(): Response<POSPrisoner?> = Response(data = null, errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)))

      fun mockPrisoner(
        nomisNumber: String,
        inOutStatus: String = "IN",
        prisonId: String = "MDI",
        lastPrisonId: String = "MDI",
        lastMovementTypeCode: String = "ADM",
        receptionDate: String = "2025-09-30",
      ) = POSPrisoner(
        prisonerNumber = nomisNumber,
        inOutStatus = inOutStatus,
        prisonId = "MDI",
        lastPrisonId = "MDI",
        lastMovementTypeCode = "ADM",
        receptionDate = "2025-09-30",
        firstName = "First",
        lastName = "Last",
        youthOffender = false,
      )

      val knownPrisonerInOutStatus = "IN"
      val knownPrisonerLastMovementTypeCode = "ADM"
      val knownPrisonerLastMovementType = LastMovementType.ADMISSION
      val knownPrisoner = mockPrisoner(nomisNumber = knownNomisNumber, inOutStatus = knownPrisonerInOutStatus, lastMovementTypeCode = knownPrisonerLastMovementTypeCode)

      beforeEach {
        Mockito.reset(prisonerOffenderSearchGateway)

        lenient().whenever(prisonerOffenderSearchGateway.getPrisonOffender(unknownNomisNumnber)).thenReturn(prisonerNotFoundErrorResponse())
        lenient().whenever(prisonerOffenderSearchGateway.getPrisonOffender(knownNomisNumber)).thenReturn(Response(data = knownPrisoner))
      }

      describe("#getPrisonerBaseLocation()") {
        it("does not return prisoner base location for unknown prisoner") {
          val response = prisonerBaseLocationGateway.getPrisonerBaseLocation(unknownNomisNumnber)

          response.data shouldBe null
          response.errors.firstOrNull().shouldNotBeNull().let {
            it.causedBy shouldBe UpstreamApi.PRISON_API
            it.type shouldBe UpstreamApiError.Type.ENTITY_NOT_FOUND
          }

          verify(prisonerOffenderSearchGateway).getPrisonOffender(unknownNomisNumnber)
        }

        it("returns prisoner base location for known prisoner") {
          val response = prisonerBaseLocationGateway.getPrisonerBaseLocation(knownNomisNumber)

          response.errors.shouldBeEmpty()
          response.data.shouldNotBeNull().let {
            it.inPrison shouldBe (knownPrisonerInOutStatus == knownPrisoner.inOutStatus)
            it.prisonId shouldBe knownPrisoner.prisonId
            it.lastPrisonId shouldBe knownPrisoner.lastPrisonId
            it.lastMovementType shouldBe knownPrisonerLastMovementType
            it.receptionDate shouldBe knownPrisoner.receptionDate?.let { LocalDate.parse(it) }
          }

          verify(prisonerOffenderSearchGateway).getPrisonOffender(knownNomisNumber)
        }
      }
    },
  )
