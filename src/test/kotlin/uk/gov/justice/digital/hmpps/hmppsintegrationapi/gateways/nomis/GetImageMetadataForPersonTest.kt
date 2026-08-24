package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.RESTAPICLIENT_FOR_PRISON_API_GATEWAY
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiImageDetail
import java.time.LocalDateTime

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonApiGateway::class],
)
class GetImageMetadataForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  @MockitoBean val restApiClient: RestApiClient,
  private val prisonApiGateway: PrisonApiGateway,
) : DescribeSpec({
    val nomisApiMockServer = ApiMockServer.create(UpstreamApi.PRISON_API)
    val offenderNo = "abc123"
    val imagePath = "/api/images/offenders/$offenderNo"
    val responseJson = """
        [
          {
            "imageId": 24213,
            "active": true,
            "captureDateTime": "2008-08-27T16:35:00",
            "imageView": "FACE",
            "imageOrientation": "FRONT",
            "imageType": "OFF_BKG"
          },
          {
            "imageId": 24299,
            "active": true,
            "captureDateTime": "2010-08-27T16:35:00",
            "imageView": "FACE",
            "imageOrientation": "FRONT",
            "imageType": "OFF_BKG"
          }
        ]
      """
    beforeEach {
      nomisApiMockServer.start()
      nomisApiMockServer.stubForGet(
        imagePath,
        responseJson,
      )

      Mockito.reset(hmppsAuthGateway)
      whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
    }

    afterTest {
      nomisApiMockServer.stop()
    }

    it("authenticates using HMPPS Auth with credentials") {
      prisonApiGateway.getImageMetadataForPerson(offenderNo)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
    }

    it("returns image metadata for the matching person ID") {
      val response = prisonApiGateway.getImageMetadataForPerson(offenderNo)

      response.data[0].id.shouldBe(24299)
      response.data[0].active.shouldBe(true)
      response.data[0].captureDateTime.shouldBe(LocalDateTime.parse("2010-08-27T16:35:00"))
      response.data[0].view.shouldBe("FACE")
      response.data[0].orientation.shouldBe("FRONT")
      response.data[0].type.shouldBe("OFF_BKG")

      response.data[1].id.shouldBe(24213)
      response.data[1].active.shouldBe(true)
      response.data[1].captureDateTime.shouldBe(LocalDateTime.parse("2008-08-27T16:35:00"))
      response.data[1].view.shouldBe("FACE")
      response.data[1].orientation.shouldBe("FRONT")
      response.data[1].type.shouldBe("OFF_BKG")
    }

    it("returns sorted by newest date image metadata for the matching person ID") {
      val response = prisonApiGateway.getImageMetadataForPerson(offenderNo)

      response.data[0].captureDateTime.shouldBe(LocalDateTime.parse("2010-08-27T16:35:00"))
      response.data[1].captureDateTime.shouldBe(LocalDateTime.parse("2008-08-27T16:35:00"))
    }

    it("returns a person without image metadata when no images are found") {
      nomisApiMockServer.stubForGet(imagePath, "[]")

      val response = prisonApiGateway.getImageMetadataForPerson(offenderNo)

      response.data.shouldBeEmpty()
    }

    it("returns an error when 404 Not Found is returned") {
      nomisApiMockServer.stubForGet(imagePath, "", HttpStatus.NOT_FOUND)

      val response = prisonApiGateway.getImageMetadataForPerson(offenderNo)

      response.errors.shouldHaveSize(1)
      response.errors
        .first()
        .causedBy
        .shouldBe(UpstreamApi.PRISON_API)
      response.errors
        .first()
        .type
        .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
    }

    it("can use the RestApiClient") {
      // Given
      val authToken = "ABC123"
      val headers = mapOf("Authorization" to "Bearer $authToken")

      val features = FeatureFlagConfig(mapOf(RESTAPICLIENT_FOR_PRISON_API_GATEWAY to true))

      val authGateway: HmppsAuthGateway = mock()
      whenever(authGateway.getClientToken("NOMIS", null)).thenReturn(authToken)

      val apiClient: RestApiClient = mock()
      whenever(apiClient.getList(eq(imagePath), eq(PrisonApiImageDetail::class), eq(headers), isNull())).thenReturn(
        RestApiResponse(
          "Test",
          HttpStatus.OK,
          RestApiClient.mapListResponse(responseJson, PrisonApiImageDetail::class),
        ),
      )

      val gateway = PrisonApiGateway("http://localhost", features, apiClient)
      gateway.hmppsAuthGateway = authGateway

      // When
      val response = gateway.getImageMetadataForPerson(offenderNo)

      // Then

      response.data[0].id.shouldBe(24299)
      response.data[0].active.shouldBe(true)
      response.data[0].captureDateTime.shouldBe(LocalDateTime.parse("2010-08-27T16:35:00"))
      response.data[0].view.shouldBe("FACE")
      response.data[0].orientation.shouldBe("FRONT")
      response.data[0].type.shouldBe("OFF_BKG")

      response.data[1].id.shouldBe(24213)
      response.data[1].active.shouldBe(true)
      response.data[1].captureDateTime.shouldBe(LocalDateTime.parse("2008-08-27T16:35:00"))
      response.data[1].view.shouldBe("FACE")
      response.data[1].orientation.shouldBe("FRONT")
      response.data[1].type.shouldBe("OFF_BKG")
    }
  })
