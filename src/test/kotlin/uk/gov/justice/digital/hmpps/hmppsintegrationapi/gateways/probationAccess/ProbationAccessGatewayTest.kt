package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.probationAccess

import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.RestClientConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.defaultObjectMapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationAccessGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ProbationAccessGateway::class],
)
@Import(RestClientConfig::class)
class ProbationAccessGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val probationAccessGateway: ProbationAccessGateway,
) : DescribeSpec(
    {
      val probationAccessMockServer = ApiMockServer.create(UpstreamApi.PROBATION_ACCESS)
      val deliusUser = "DELIUS_USER"
      val crns = listOf("A888888", "A777777", "A666666")

      beforeEach {
        probationAccessMockServer.start()
        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("PROBATION_ACCESS", null)).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )

        probationAccessMockServer.stubForPost(
          "/user/$deliusUser/access",
          defaultObjectMapper.writeValueAsString(crns),
          File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/probationAccess/fixtures/CheckUserAccessResponse.json").readText(),
        )
      }

      afterTest {
        probationAccessMockServer.stop()
      }

      describe("POST /user/$deliusUser/access ") {
        it("returns a successful probation access response") {
          val probationAccessResponse = probationAccessGateway.getProbationAccess(deliusUser, crns, null)
          assertThat(probationAccessResponse.data).isNotNull
          assertThat(probationAccessResponse.errors).isNullOrEmpty()
        }

        it("returns an error") {
          probationAccessMockServer.stubForPost(
            "/user/$deliusUser/access",
            defaultObjectMapper.writeValueAsString(crns),
            "",
            HttpStatus.BAD_REQUEST,
          )
          val probationAccessResponse = probationAccessGateway.getProbationAccess(deliusUser, crns, null)
          assertThat(probationAccessResponse.data).isNull()
          assertThat(probationAccessResponse.errors).isNotEmpty
        }
      }
    },
  )
