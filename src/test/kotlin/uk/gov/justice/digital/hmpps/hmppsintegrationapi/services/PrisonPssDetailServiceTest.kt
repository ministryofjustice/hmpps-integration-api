package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiPssDetail
import kotlin.test.Test

class PrisonPssDetailServiceTest {
  val prisonApiGateway: PrisonApiGateway = mock(PrisonApiGateway::class.java)
  val personService: GetPersonService = mock(GetPersonService::class.java)
  val service: PrisonPssDetailService = PrisonPssDetailService(prisonApiGateway, personService)
  val nomsId = "A1417AE"
  val requestContext = buildRequestContext()
  val pssDetail =
    PrisonApiPssDetail(
      type = "IEP_CHANGED",
      id = 21,
      noms_id = nomsId,
      prison_id = "BMI",
      timestamp = "2016-10-21 15:55:06.284",
      offender_details_request =
        mapOf(
          "offender_details" to
            mapOf(
              "personal_details" to
                mapOf("offender_surname" to "SMITH", "offender_given_name_1" to "JOHN", "offender_dob" to "2000-02-20 00:00:00"),
            ),
        ),
    )

  @Test
  fun `should get pss detail for a prisoner`() {
    whenever(prisonApiGateway.getPssDetail(nomsId, requestContext)).thenReturn(
      Response(data = pssDetail),
    )
    whenever(personService.getNomisNumber(nomsId, requestContext.filters)).thenReturn(Response(data = NomisNumber(nomsId)))
    val response = service.pssDetail(nomsId, requestContext)
    response.errors.shouldBeEmpty()
    val offenderDetails = response.data?.offender_details_request!!["offender_details"] as Map<*, *>
    val personalDetails = offenderDetails["personal_details"] as Map<*, *>
    personalDetails["offender_surname"] shouldBe "SMITH"
    personalDetails["offender_given_name_1"] shouldBe "JOHN"
    personalDetails["offender_dob"] shouldBe "2000-02-20 00:00:00"
  }

  @Test
  fun `returns a 404 when person service returns 404`() {
    whenever(prisonApiGateway.getPssDetail(nomsId, requestContext)).thenReturn(
      Response(data = pssDetail),
    )
    whenever(personService.getNomisNumber(nomsId, requestContext.filters)).thenReturn(Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND))))
    val response = service.pssDetail(nomsId, requestContext)
    response.errors.shouldNotBeEmpty()
    response.errors.shouldContain(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND))
  }

  @Test
  fun `returns a 404 when prison api gateway returns 404`() {
    whenever(personService.getNomisNumber(nomsId, requestContext.filters)).thenReturn(Response(data = NomisNumber(nomsId)))
    whenever(prisonApiGateway.getPssDetail(nomsId, requestContext)).thenReturn(Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND))))
    val response = service.pssDetail(nomsId, requestContext)
    response.errors.shouldNotBeEmpty()
    response.errors.shouldContain(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND))
  }
}
