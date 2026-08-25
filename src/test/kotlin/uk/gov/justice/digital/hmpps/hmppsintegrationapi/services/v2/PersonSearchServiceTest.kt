package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.v2

import io.kotest.matchers.equals.shouldEqual
import io.kotest.matchers.shouldBe
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CorePersonRecordGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CPRName
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CorePersonRecordSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CorePersonRecordSearchResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CorePersonRecordSearchResponseItem
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import kotlin.test.Test

class PersonSearchServiceTest {
  val corePersonRecordGateway: CorePersonRecordGateway = mock(CorePersonRecordGateway::class.java)
  val service = PersonSearchService(corePersonRecordGateway)
  val requestContext = buildRequestContext()
  val successResponse = CorePersonRecordSearchResponse(data = listOf(CorePersonRecordSearchResponseItem(CPRName("John", "Brian", "Doe"))))

  @Test
  fun `should successfully search for a person`() {
    val request = CorePersonRecordSearchRequest(firstName = "John", lastName = "Doe")
    whenever(corePersonRecordGateway.corePersonRecordSearch(request, requestContext)).thenReturn(Response(successResponse))
    val response = service.personSearch(request, requestContext)
    response.data
      ?.get(0)
      ?.name
      ?.firstName shouldEqual "John"
    response.data
      ?.get(0)
      ?.name
      ?.middleNames shouldEqual "Brian"
    response.data
      ?.get(0)
      ?.name
      ?.lastName shouldEqual "Doe"
  }

  @Test
  fun `should return a bad request`() {
    val request = CorePersonRecordSearchRequest(firstName = "John")
    whenever(corePersonRecordGateway.corePersonRecordSearch(request, requestContext))
      .thenReturn(Response(null, listOf(UpstreamApiError(UpstreamApi.CORE_PERSON_RECORD, UpstreamApiError.Type.BAD_REQUEST))))
    val response = service.personSearch(request, requestContext)
    response.data shouldBe null
    response.errors shouldBe listOf(UpstreamApiError(UpstreamApi.CORE_PERSON_RECORD, UpstreamApiError.Type.BAD_REQUEST))
  }
}
