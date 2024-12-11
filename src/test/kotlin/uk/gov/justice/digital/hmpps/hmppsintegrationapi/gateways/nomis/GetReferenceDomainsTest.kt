package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NomisApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NomisGateway::class],
)
class GetReferenceDomainsTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val nomisGateway: NomisGateway,
) : DescribeSpec(
    {
      val nomisApiMockServer = NomisApiMockServer()
      val testDomain = "abc"
      val domainPath = "/api/reference-domains/domains/$testDomain/codes"
      beforeEach {
        nomisApiMockServer.start()
        nomisApiMockServer.stubNomisApiResponse(
          domainPath,
          """
          [
            {"domain":"abc", "code":"a"},
            {"domain":"abc", "code":"b"},
            {"domain":"abc", "code":"c"}
          ]
        """,
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        nomisApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        nomisGateway.getReferenceDomains(testDomain)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
      }

      it("returns reference domains with the matching ID") {
        val response = nomisGateway.getReferenceDomains(testDomain)

        response.data.count().shouldBe(3)
        response.data.count { it.domain == "abc" && it.code == "a" }.shouldBe(1)
        response.data.count { it.domain == "abc" && it.code == "b" }.shouldBe(1)
        response.data.count { it.domain == "abc" && it.code == "c" }.shouldBe(1)
      }

      it("returns an empty list when no domains are found") {
        nomisApiMockServer.stubNomisApiResponse(domainPath, "[]")

        val response = nomisGateway.getReferenceDomains(testDomain)

        response.data.shouldBeEmpty()
      }

      it("returns an error when 404 NOT FOUND is returned") {
        nomisApiMockServer.stubNomisApiResponse(
          domainPath,
          """
        {
          "developerMessage": "cannot find person"
        }
        """,
          HttpStatus.NOT_FOUND,
        )

        val response = nomisGateway.getReferenceDomains(testDomain)

        response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
      }
    },
  )
