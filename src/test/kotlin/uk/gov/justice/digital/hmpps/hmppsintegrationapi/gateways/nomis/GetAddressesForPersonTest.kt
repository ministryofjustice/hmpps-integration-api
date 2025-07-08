package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonApiGateway::class],
)
class GetAddressesForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val prisonApiGateway: PrisonApiGateway,
) : DescribeSpec(
    {
      val nomisApiMockServer = ApiMockServer.create(UpstreamApi.PRISON_API)
      val offenderNo = "abc123"
      val addressPath = "/api/offenders/$offenderNo/addresses"
      beforeEach {
        nomisApiMockServer.start()
        nomisApiMockServer.stubForGet(
          addressPath,
          """
          [
            {
              "postalCode": "SE1 1TZ",
              "addressId": 123456,
              "addressType": "BUS",
              "flat": "89",
              "premise": "The chocolate factory",
              "street": "Omeara",
              "locality": "London Bridge",
              "town": "London Town",
              "county": "Greater London",
              "country": "England",
              "comment": "this is a comment text",
              "primary": false,
              "noFixedAddress": false,
              "startDate": "2021-05-01",
              "endDate": "2023-05-20",
              "phones": [],
              "addressUsages": [
                {
                  "addressId": 8681879,
                  "addressUsage": "A99",
                  "addressUsageDescription": "Chocolate Factory",
                  "activeFlag": false
                },
                {
                  "addressId": 8681811,
                  "addressUsageDescription": "Do not use. No addressUsage code",
                  "activeFlag": false
                },
                {
                  "addressId": 8681879,
                  "addressUsage": "B99",
                  "addressUsageDescription": "Glass Elevator",
                  "activeFlag": false
                }
              ]
            }
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
        prisonApiGateway.getAddressesForPerson(offenderNo)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
      }

      it("returns addresses for a person with the matching ID") {
        val response = prisonApiGateway.getAddressesForPerson(offenderNo)

        response.data.count().shouldBeGreaterThan(0)
      }

      it("returns only the usages that have an addressUsage code") {
        val response = prisonApiGateway.getAddressesForPerson(offenderNo)

        response.data[0]
          .types
          .count()
          .shouldBeExactly(3)
        response.data[0]
          .types[0]
          .code
          .shouldBe("A99")
        response.data[0]
          .types[1]
          .code
          .shouldBe("B99")
        response.data[0]
          .types[2]
          .code
          .shouldBe("BUS")
      }

      it("returns an empty list when no addresses are found") {
        nomisApiMockServer.stubForGet(addressPath, "[]")

        val response = prisonApiGateway.getAddressesForPerson(offenderNo)

        response.data.shouldBeEmpty()
      }

      it("returns an error when 404 NOT FOUND is returned") {
        nomisApiMockServer.stubForGet(
          addressPath,
          """
        {
          "developerMessage": "cannot find person"
        }
        """,
          HttpStatus.NOT_FOUND,
        )

        val response = prisonApiGateway.getAddressesForPerson(addressPath)

        response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
      }
    },
  )
