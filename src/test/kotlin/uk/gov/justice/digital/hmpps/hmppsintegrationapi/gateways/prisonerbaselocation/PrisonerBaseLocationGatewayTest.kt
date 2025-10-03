package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.prisonerbaselocation

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerBaseLocationGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LastMovementType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import java.io.File
import java.time.LocalDate

internal const val FIXTURES_DIR = "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/prisonerbaselocation/fixtures"

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonerBaseLocationGateway::class, PrisonerOffenderSearchGateway::class, FeatureFlagConfig::class],
)
class PrisonerBaseLocationGatewayTest(
  @MockitoBean private val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  private val prisonerBaseLocationGateway: PrisonerBaseLocationGateway,
) : DescribeSpec(
    {

      describe("Prisoner Base Location Gateway") {
        fun readFixtures(fileName: String): String = File("$FIXTURES_DIR/$fileName").readText()
        val knownNomisNumber = "A1234BC"
        val unknownNomisNumber = "Z9876YX"

        describe("when prisoner base location api is enabled") {
          val prisonerBaseLocationApiMockServer = ApiMockServer.create(UpstreamApi.PRISONER_BASE_LOCATION)
          val knownPrisonerResponse = readFixtures("PrisonerBaseLocationResponse.json")

          beforeTest {
            whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.USE_PRISONER_BASE_LOCATION_API)).thenReturn(true)
            prisonerBaseLocationApiMockServer.start()
          }

          beforeEach {
            whenever(hmppsAuthGateway.getClientToken("Prisoner Base Location")).thenReturn(HmppsAuthMockServer.TOKEN)

            with(prisonerBaseLocationApiMockServer) {
              stubForGet(
                path = "/v1/persons/$unknownNomisNumber/prisoner-base-location",
                status = HttpStatus.NOT_FOUND,
                body = "",
              )

              stubForGet(
                path = "/v1/persons/$knownNomisNumber/prisoner-base-location",
                body = knownPrisonerResponse,
              )
            }
          }

          afterTest {
            prisonerBaseLocationApiMockServer.stop()
            prisonerBaseLocationApiMockServer.resetValidator()
          }

          describe("#getPrisonerBaseLocation()") {
            it("does not return prisoner base location for unknown prisoner") {
              val response = prisonerBaseLocationGateway.getPrisonerBaseLocation(unknownNomisNumber)

              response.data shouldBe null
              response.errors.firstOrNull().shouldNotBeNull().let {
                it.causedBy shouldBe UpstreamApi.PRISONER_BASE_LOCATION
                it.type shouldBe UpstreamApiError.Type.ENTITY_NOT_FOUND
              }
            }

            it("returns prisoner base location for known prisoner") {
              val response = prisonerBaseLocationGateway.getPrisonerBaseLocation(knownNomisNumber)

              response.errors.shouldBeEmpty()
              response.data.shouldNotBeNull().let {
                it.inPrison shouldBe true
                it.prisonId shouldBe "MDI"
                it.lastPrisonId shouldBe "MDI"
                it.lastMovementType shouldBe LastMovementType.ADMISSION
                it.receptionDate shouldBe LocalDate.parse("2025-04-01")
              }

              prisonerBaseLocationApiMockServer.assertValidationPassed()
            }
          }
        }

        describe("when prisoner offender search api is disabled") {
          val prisonerOffenderSearchApiMockServer = ApiMockServer.create(UpstreamApi.PRISONER_OFFENDER_SEARCH)

          fun mockPrisoner(
            nomisNumber: String,
            inOutStatus: String = "IN",
            prisonId: String = "MDI",
            lastPrisonId: String = "MDI",
            lastMovementTypeCode: String = "ADM",
            receptionDate: String = "2023-05-01",
          ) = POSPrisoner(
            prisonerNumber = nomisNumber,
            inOutStatus = inOutStatus,
            prisonId = prisonId,
            lastPrisonId = lastPrisonId,
            lastMovementTypeCode = lastMovementTypeCode,
            receptionDate = receptionDate,
            firstName = "First",
            lastName = "Last",
            youthOffender = false,
          )

          val knownPrisonerInOutStatus = "IN"
          val knownPrisonerLastMovementTypeCode = "ADM"
          val knownPrisonerLastMovementType = LastMovementType.ADMISSION
          val knownPrisoner = mockPrisoner(nomisNumber = knownNomisNumber, inOutStatus = knownPrisonerInOutStatus, lastMovementTypeCode = knownPrisonerLastMovementTypeCode)
          val knownPrisonerResponse = readFixtures("PrisonerByIdResponse.json")
          val unknownPrisonerResponse = readFixtures("PrisonerByIdNotFoundResponse.json")

          beforeTest {
            whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.USE_PRISONER_BASE_LOCATION_API)).thenReturn(false)
            prisonerOffenderSearchApiMockServer.start()
          }

          beforeEach {
            whenever(hmppsAuthGateway.getClientToken("Prisoner Offender Search")).thenReturn(HmppsAuthMockServer.TOKEN)

            with(prisonerOffenderSearchApiMockServer) {
              stubForGet(
                path = "/prisoner/$unknownNomisNumber",
                status = HttpStatus.NOT_FOUND,
                body = unknownPrisonerResponse,
              )

              stubForGet(
                path = "/prisoner/$knownNomisNumber",
                body = knownPrisonerResponse,
              )
            }
          }

          afterTest {
            prisonerOffenderSearchApiMockServer.stop()
            prisonerOffenderSearchApiMockServer.resetValidator()
          }

          describe("#getPrisonerBaseLocation()") {
            it("does not return prisoner base location for unknown prisoner") {
              val response = prisonerBaseLocationGateway.getPrisonerBaseLocation(unknownNomisNumber)

              response.data shouldBe null
              response.errors.firstOrNull().shouldNotBeNull().let {
                it.causedBy shouldBe UpstreamApi.PRISONER_OFFENDER_SEARCH
                it.type shouldBe UpstreamApiError.Type.ENTITY_NOT_FOUND
              }
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

              prisonerOffenderSearchApiMockServer.assertValidationPassed()
            }
          }
        }
      }
    },
  )
