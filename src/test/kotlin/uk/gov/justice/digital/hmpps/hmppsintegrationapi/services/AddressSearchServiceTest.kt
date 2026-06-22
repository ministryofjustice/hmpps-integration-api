package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AddressSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.PSAddress
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.PSAddressSearchResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.PSPerson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.PSPersonAddress
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.PSStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.PSType

class AddressSearchServiceTest {
  lateinit var addressSearchService: AddressSearchService
  val probationOffenderSearchGateway: ProbationOffenderSearchGateway = mock()
  val requestContext: RequestContext = buildRequestContext()
  val gwSearchResponsePersonItem =
    PSPerson(
      id = 1234,
      crn = "X123456",
    )
  val gwSearchResponseItemAddress =
    PSAddress(
      id = 123,
      buildingName = "Burnham House",
      addressNumber = "1",
      streetName = "Church Road",
      district = "Clarendon Park",
      town = "Leicester",
      county = "Leicestershire",
      postcode = "LM2 1BF",
      startDate = "2020-08-03",
      notes = "notes text",
      createdDateTime = "2020-08-11T11:00:01+01:00",
      lastUpdatedDateTime = "2022-06-09T11:47:51+01:00",
      status =
        PSStatus(
          code = "M",
          description = "Main",
        ),
      type =
        PSType(
          code = "A02",
          description = "Approved Premises",
        ),
      noFixedAbode = false,
    )
  val gwSearchResponseItem = PSPersonAddress(gwSearchResponsePersonItem, gwSearchResponseItemAddress, 100)
  val gwResponse =
    PSAddressSearchResponse(
      listOf(gwSearchResponseItem),
    )

  @BeforeEach
  fun setup() {
    whenever(probationOffenderSearchGateway.addressSearch(any(), any(), any())).thenReturn(Response(gwResponse))
    addressSearchService = AddressSearchService(probationOffenderSearchGateway)
  }

  @Test
  fun `returns a response`() {
    val response = probationOffenderSearchGateway.addressSearch(AddressSearchRequest("Burnham House"), 100, requestContext)
    response.data?.shouldBe(gwResponse)
  }

  @Test
  fun `returns a bad request from gateway response`() {
    val expectedError = UpstreamApiError(type = UpstreamApiError.Type.BAD_REQUEST, causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH)
    whenever(probationOffenderSearchGateway.addressSearch(any(), any(), any())).thenReturn(
      Response(
        errors = listOf(expectedError),
        data = null,
      ),
    )
    val response = probationOffenderSearchGateway.addressSearch(AddressSearchRequest(), 100, buildRequestContext())
    response.errors.size.shouldBe(1)
    response.errors.shouldBe(listOf(expectedError))
  }
}
