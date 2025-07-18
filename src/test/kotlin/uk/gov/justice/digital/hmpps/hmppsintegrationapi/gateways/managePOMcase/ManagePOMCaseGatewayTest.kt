package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.managePOMcase

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManagePOMCaseGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ManagePOMCaseGateway::class],
)
class ManagePOMCaseGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val managePOMCaseGateway: ManagePOMCaseGateway,
) : DescribeSpec(
    {
      val nomsNumber = "X1234YZ"
      val path = "/api/allocation/$nomsNumber/primary_pom"
      val managePOMCaseApiMockServer = ApiMockServer.create(UpstreamApi.MANAGE_POM_CASE)

      beforeEach {
        managePOMCaseApiMockServer.start()
        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("ManagePOMCase")).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )
      }

      afterTest {
        managePOMCaseApiMockServer.stop()
        managePOMCaseApiMockServer.resetValidator()
      }

      it("authenticates using HMPPS Auth with credentials") {
        managePOMCaseGateway.getPrimaryPOMForNomisNumber(nomsNumber)
        verify(hmppsAuthGateway, times(1)).getClientToken("ManagePOMCase")
      }

      it("upstream API returns an error, throw exception") {
        managePOMCaseApiMockServer.stubForGet(path, "", HttpStatus.BAD_REQUEST)

        val response =
          shouldThrow<WebClientResponseException> {
            managePOMCaseGateway.getPrimaryPOMForNomisNumber(nomsNumber)
          }
        response.statusCode.shouldBe(HttpStatus.BAD_REQUEST)
      }

      it("returns primary offender officer") {
        managePOMCaseApiMockServer.stubForGet(
          path,
          File(
            "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/managePOMcase/fixtures/GetPrimaryPOMResponse.json",
          ).readText(),
        )

        val response = managePOMCaseGateway.getPrimaryPOMForNomisNumber(nomsNumber)
        response.data.shouldNotBeNull()
        response.data.forename.shouldBe("Joe")
        response.data.surname.shouldBe("Bloggs")

        managePOMCaseApiMockServer.assertValidationPassed()
      }
    },
  )
