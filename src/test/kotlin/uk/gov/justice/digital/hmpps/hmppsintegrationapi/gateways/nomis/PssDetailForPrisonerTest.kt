package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError.Type
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiPssDetail
import java.io.File

class PssDetailForPrisonerTest :
  DescribeSpec(
    {
      val nomisApiMockServer = ApiMockServer.create(UpstreamApi.PRISON_API)
      val nomisId = "A1417AE"
      val path = "/api/v1/offenders/$nomisId/pss-detail"
      val authToken = "ABC123"
      val responseJson =
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/PssDetailResponse.json",
        ).readText()
      val requestContext = buildRequestContext("testUser")
      val apiClient: RestApiClient = mock()
      val features = FeatureFlagConfig()
      val authGateway: HmppsAuthGateway = mock()
      val gateway = PrisonApiGateway("http://localhost", features, apiClient)

      beforeEach {
        nomisApiMockServer.start()
        Mockito.reset(authGateway)
        gateway.hmppsAuthGateway = authGateway
        whenever(authGateway.getClientToken("NOMIS", null)).thenReturn(authToken)
      }

      afterTest {
        nomisApiMockServer.stop()
      }

      it("returns PSS Detail for an offender") {
        whenever(apiClient.get(eq(path), eq(PrisonApiPssDetail::class), any(), isNull())).thenReturn(
          RestApiResponse(
            "Test",
            HttpStatus.OK,
            RestApiClient.mapResponse(responseJson, PrisonApiPssDetail::class),
          ),
        )

        val response = gateway.getPssDetail(nomisId, requestContext)

        response.errors.shouldBeEmpty()
        response.data?.type.shouldBe("OFFENDER_DETAILS_REQUEST")
        response.data?.id.shouldBe(21)
        response.data?.noms_id.shouldBe("A1417AE")
        response.data?.prison_id.shouldBe("BMI")
        response.data?.timestamp.shouldBe("2016-10-21 15:55:06.284")

        val offenderDetailsRequest =
          response.data?.offender_details_request!!
        val offenderDetails = offenderDetailsRequest["offender_details"] as Map<*, *>
        val personalDetails = offenderDetails["personal_details"] as Map<*, *>

        personalDetails["offender_surname"] shouldBe ("SMITH")
        personalDetails["offender_given_name_1"] shouldBe ("JOHN")
        personalDetails["offender_dob"] shouldBe ("2000-02-20 00:00:00")
      }

      it("returns a 404 for an offender") {
        whenever(apiClient.get(eq(path), eq(PrisonApiPssDetail::class), any(), isNull())).thenReturn(
          RestApiResponse(
            "Test",
            HttpStatus.NOT_FOUND,
            null,
            errors = listOf(WebClientResponseException(404, "Not Found", null, null, null, null)),
          ),
        )
        val response = gateway.getPssDetail(nomisId, requestContext)
        response.errors.shouldNotBeEmpty()
        response.errors.shouldContain(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = Type.ENTITY_NOT_FOUND, description = "404 Not Found"))
      }
    },
  )
