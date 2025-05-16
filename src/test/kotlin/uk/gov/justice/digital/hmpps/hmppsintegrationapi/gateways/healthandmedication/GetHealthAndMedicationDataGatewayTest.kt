package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.healthandmedication

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HealthAndMedicationGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [HealthAndMedicationGateway::class],
)
class GetHealthAndMedicationDataGatewayTest(
  private val healthAndMedicationGateway: HealthAndMedicationGateway,
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
) : DescribeSpec({
    val healthAndMedicationMockServer = ApiMockServer.create(UpstreamApi.HEALTH_AND_MEDICATION)
    val hmppsId = "A1234AA"

    beforeEach {
      healthAndMedicationMockServer.start()
      Mockito.reset(hmppsAuthGateway)

      whenever(hmppsAuthGateway.getClientToken("HEALTH_AND_MEDICATION")).thenReturn(HmppsAuthMockServer.TOKEN)
    }

    afterTest {
      healthAndMedicationMockServer.stop()
    }

    it("authenticates using HMPPS Auth with credentials") {
      healthAndMedicationGateway.getHealthAndMedicationData(hmppsId)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("HEALTH_AND_MEDICATION")
    }

    it("returns a 200 with the data when data is found") {
      healthAndMedicationMockServer.stubForGet(
        "/prisoners/$hmppsId",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/healthandmedication/fixtures/GetHealthAndMedicationResponse.json",
        ).readText(),
      )

      val result = healthAndMedicationGateway.getHealthAndMedicationData(hmppsId)
      result.errors.size.shouldBe(0)
      result.data.shouldNotBeNull()
      result.data.dietAndAllergy.foodAllergies.value.size
        .shouldBe(1)
      result.data.dietAndAllergy.medicalDietaryRequirements.value.size
        .shouldBe(1)
      result.data.dietAndAllergy.personalisedDietaryRequirements.value.size
        .shouldBe(1)
      result.data.dietAndAllergy.cateringInstructions.value
        .shouldBe("catering instructions")
    }

    it("returns an error when a 400 status code is returned") {
      healthAndMedicationMockServer.stubForGet(
        "/prisoners/$hmppsId",
        "",
        HttpStatus.BAD_REQUEST,
      )

      val result = healthAndMedicationGateway.getHealthAndMedicationData(hmppsId)
      result.data.shouldBeNull()
      result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.HEALTH_AND_MEDICATION, UpstreamApiError.Type.BAD_REQUEST)))
    }
  })
