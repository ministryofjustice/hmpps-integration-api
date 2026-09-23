package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cemo.CemoOrderStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.util.UUID

@ActiveProfiles("test")
@TestPropertySource(properties = ["services.cemo.base-url=http://localhost:4037"])
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [CemoGateway::class],
)
class CemoGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val cemoGateway: CemoGateway,
) : io.kotest.core.spec.style.DescribeSpec(
    {
      val cemoMockServer = ApiMockServer.create(UpstreamApi.CEMO)
      val orderId = UUID.fromString("11111111-1111-1111-1111-111111111111")
      val path = "/api/orders/search/by-case-id/case-123"

      fun version(
        versionId: Int,
        status: CemoOrderStatus,
      ): String =
        """
        {
          "id": "22222222-2222-2222-2222-222222222222",
          "versionId": $versionId,
          "additionalDocuments": [],
          "addresses": [],
          "curfewTimeTable": [],
          "dapoClauses": [],
          "enforcementZoneConditions": [],
          "isValid": true,
          "mandatoryAttendanceConditions": [],
          "offences": [],
          "status": "${status.name}",
          "type": "REQUEST",
          "username": "test-user",
          "dataDictionaryVersion": "DDV5"
        }
        """.trimIndent()

      beforeEach {
        cemoMockServer.start()
        reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("CEMO")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        cemoMockServer.stop()
      }

      it("gets an order by case id using the CEMO client token") {
        cemoMockServer.stubForGet(
          path,
          """
          {
            "id": "$orderId",
            "versions": [
              ${version(1, CemoOrderStatus.IN_PROGRESS)},
              ${version(2, CemoOrderStatus.SUBMITTED)}
            ]
          }
          """,
        )

        val response = cemoGateway.getOrderByCaseId("case-123")

        response.data.shouldNotBeNull().id shouldBe orderId
        response.data.versions.map { it.status } shouldBe listOf(CemoOrderStatus.IN_PROGRESS, CemoOrderStatus.SUBMITTED)
        verify(hmppsAuthGateway).getClientToken("CEMO")
        cemoMockServer.verify(
          getRequestedFor(urlEqualTo(path)),
        )
        cemoMockServer.assertValidationPassed()
      }

      it("returns an entity-not-found error when CEMO returns 404") {
        cemoMockServer.stubForGet(path, "", HttpStatus.NOT_FOUND)

        val response = cemoGateway.getOrderByCaseId("case-123")

        response.data shouldBe null
        response.errors.single().type shouldBe UpstreamApiError.Type.ENTITY_NOT_FOUND
      }
    },
  )
