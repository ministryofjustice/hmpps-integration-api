package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ndelius

import io.kotest.core.spec.style.DescribeSpec
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NDeliusGateway::class, FeatureFlagConfig::class],
)
class GetPersonExistsTest(
  @MockitoBean
  val hmppsAuthGateway: HmppsAuthGateway,
  val nDeliusGateway: NDeliusGateway,
) : DescribeSpec(
    {
      val deliusCrn = "X777776"
      val path = "/exists-in-delius/crn/$deliusCrn"
      val nDeliusApiMockServer = ApiMockServer.create(UpstreamApi.NDELIUS)

      beforeEach {
        nDeliusApiMockServer.start()
        nDeliusApiMockServer.stubForGet(
          path,
          File(
            "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/ndelius/fixtures/GetPersonExistsResponse.json",
          ).readText(),
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("nDelius")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        nDeliusApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        nDeliusGateway.getPersonExists(deliusCrn)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("nDelius")
      }

      it("returns matching person as true with matching CRN") {
        val response = nDeliusGateway.getPersonExists(deliusCrn)

        response.crn.shouldBe(deliusCrn)
        response.existsInDelius.shouldBe(true)
      }

      it("returns matching person as false with matching CRN") {
        nDeliusApiMockServer.stubForGet(
          path,
          """
            {
              "crn": "X777776",
              "existsInDelius": false
            }
          """,
        )

        val response = nDeliusGateway.getPersonExists(deliusCrn)

        response.crn.shouldBe(deliusCrn)
        response.existsInDelius.shouldBe(false)
      }

      it("returns matching person as false with 404 response") {
        nDeliusApiMockServer.stubForGet(path, "", HttpStatus.NOT_FOUND)

        val response = nDeliusGateway.getPersonExists(deliusCrn)

        response.crn.shouldBe(deliusCrn)
        response.existsInDelius.shouldBe(false)
      }
    },
  )
