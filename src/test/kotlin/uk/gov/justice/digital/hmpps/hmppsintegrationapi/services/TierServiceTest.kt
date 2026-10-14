package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.matchers.shouldBe
import jakarta.validation.ValidationException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.TierGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.tier.TierV3DetailsDto

class TierServiceTest {
  lateinit var tierService: TierService
  val tierGateway: TierGateway = mock()
  val personService: GetPersonService = mock()
  val requestContext = buildRequestContext()
  val crn = "X123456"
  val nomsNumber = "A1234BC"
  val tierCalculationResult = TierV3DetailsDto(calculationId = "ID")

  @BeforeEach
  fun setup() {
    whenever(tierGateway.getTierDetails(crn, requestContext)).thenReturn(
      Response(tierCalculationResult),
    )
    whenever(personService.convert(nomsNumber, GetPersonService.IdentifierType.CRN)).thenReturn(
      Response(crn),
    )
    tierService = TierService(tierGateway, personService)
  }

  @Test
  fun `returns a tier calculation for a CRN`() {
    val response = tierService.getTierDetails(crn, requestContext)
    response shouldBe Response(tierCalculationResult)
  }

  @Test
  fun `returns a tier calculation for a NOMIS`() {
    val response = tierService.getTierDetails(nomsNumber, requestContext)
    response shouldBe Response(tierCalculationResult)
  }

  @Test
  fun `throws a bad request when invalid CRN`() {
    val exception = assertThrows<ValidationException> { tierService.getTierDetails("1234", requestContext) }
    exception.message shouldBe "Invalid HMPPS identifier: 1234"
  }

  @Test
  fun `returns an error when converting noms number to CRN returns a failure`() {
    whenever(personService.convert(nomsNumber, GetPersonService.IdentifierType.CRN)).thenReturn(
      Response(null, listOf(UpstreamApiError(causedBy = UpstreamApi.TIER, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))),
    )
    val response = tierService.getTierDetails(nomsNumber, requestContext)
    response shouldBe Response(null, listOf(UpstreamApiError(causedBy = UpstreamApi.TIER, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)))
  }
}
