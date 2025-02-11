package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThan
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NomisApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NomisGateway::class],
)
class GetOffenderVisitRestrictionsTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  val nomisGateway: NomisGateway,
) : DescribeSpec(
    {
      val nomisApiMockServer = NomisApiMockServer()
      val offenderNo = "zyx987"
      val offenderRestrictionsPath = "/api/offenders/$offenderNo/offender-restrictions"

      beforeEach {
        nomisApiMockServer.start()
        nomisApiMockServer.stubNomisApiResponse(
          offenderRestrictionsPath,
          """
            {
              "bookingId": 9007199254740991,
              "offenderRestrictions": [
                {
                  "restrictionId": 9007199254740991,
                  "comment": "string",
                  "restrictionType": "string",
                  "restrictionTypeDescription": "string",
                  "startDate": "1980-01-01",
                  "expiryDate": "1980-01-01",
                  "active": true
                }
              ]
            }
        """.removeWhitespaceAndNewlines(),
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        nomisApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        nomisGateway.getOffenderVisitRestrictions(offenderNo)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
      }

      it("returns offender visit restrictions for the matching person ID") {
        val response = nomisGateway.getOffenderVisitRestrictions(offenderNo)

        response.data.shouldNotBeNull()
        response.data!!.count().shouldBeGreaterThan(0)
      }

      it("returns a person with an empty list of restrictions when no restrictions are found") {
        nomisApiMockServer.stubNomisApiResponse(
          offenderRestrictionsPath,
          """
            {
              "bookingId": 9007199254740991,
              "offenderRestrictions": []
            }
            """.removeWhitespaceAndNewlines(),
        )

        val response = nomisGateway.getOffenderVisitRestrictions(offenderNo)

        response.data.shouldBeEmpty()
      }

      it("returns an error when 404 Not Found is returned because no person is found") {
        nomisApiMockServer.stubNomisApiResponse(offenderRestrictionsPath, "", HttpStatus.NOT_FOUND)

        val response = nomisGateway.getOffenderVisitRestrictions(offenderNo)

        response.errors.shouldHaveSize(1)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.NOMIS)
        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }

      it("returns an error when 400 Bad Request is returned because of an invalid request") {
        nomisApiMockServer.stubNomisApiResponse(offenderRestrictionsPath, "", HttpStatus.BAD_REQUEST)

        val response = nomisGateway.getOffenderVisitRestrictions(offenderNo)

        response.errors.shouldHaveSize(1)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.NOMIS)
        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.BAD_REQUEST)
      }
    },
  )
