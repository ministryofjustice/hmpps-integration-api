package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.data.web.PagedModel
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PRContactSearchResponseItem
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PRPaginatedContactSearchResponse

class ContactSearchServiceTest {
  lateinit var contactSearchService: ContactSearchService
  val personalRelationshipsGateway: PersonalRelationshipsGateway = mock()
  val requestContext: RequestContext = buildRequestContext()
  val gwSearchResponseItem = PRContactSearchResponseItem(1234, "John", "James", "Smith", dateOfBirth = "01-10-1990")
  val gwResponse =
    PRPaginatedContactSearchResponse(
      contacts = listOf(gwSearchResponseItem),
      pageMetadata = PagedModel.PageMetadata(10, 0, 1, 1),
    )

  @BeforeEach
  fun setup() {
    whenever(personalRelationshipsGateway.contactSearch(any(), any(), any(), any())).thenReturn(Response(gwResponse))
    contactSearchService = ContactSearchService(personalRelationshipsGateway)
  }

  @Test
  fun `returns a response`() {
    val response = contactSearchService.contactSearch(ContactSearchRequest("John", lastName = "Smith"), 1, 10, requestContext)
    response.data?.content?.shouldBe(listOf(gwSearchResponseItem.toContact()))
  }

  @Test
  fun `returns a bad request from gateway response`() {
    val expectedError = UpstreamApiError(type = UpstreamApiError.Type.BAD_REQUEST, causedBy = UpstreamApi.PERSONAL_RELATIONSHIPS)
    whenever(personalRelationshipsGateway.contactSearch(any(), any(), any(), any())).thenReturn(
      Response(
        errors = listOf(expectedError),
        data = null,
      ),
    )
    val response = contactSearchService.contactSearch(ContactSearchRequest(), 0, 10, buildRequestContext())
    response.errors.size.shouldBe(1)
    response.errors.shouldBe(listOf(expectedError))
  }
}
