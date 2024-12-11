package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan
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
class GetAddressesForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val nomisGateway: NomisGateway,
) : DescribeSpec(
    {
      val nomisApiMockServer = NomisApiMockServer()
      val offenderNo = "abc123"
      val addressPath = "/api/offenders/$offenderNo/addresses"
      beforeEach {
        nomisApiMockServer.start()
        nomisApiMockServer.stubNomisApiResponse(
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
        nomisGateway.getAddressesForPerson(offenderNo)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
      }

      it("returns addresses for a person with the matching ID") {
        val response = nomisGateway.getAddressesForPerson(offenderNo)

        response.data.count().shouldBeGreaterThan(0)
      }

      it("returns an empty list when no addresses are found") {
        nomisApiMockServer.stubNomisApiResponse(addressPath, "[]")

        val response = nomisGateway.getAddressesForPerson(offenderNo)

        response.data.shouldBeEmpty()
      }

      it("returns an error when 404 NOT FOUND is returned") {
        nomisApiMockServer.stubNomisApiResponse(
          addressPath,
          """
        {
          "developerMessage": "cannot find person"
        }
        """,
          HttpStatus.NOT_FOUND,
        )

        val response = nomisGateway.getAddressesForPerson(addressPath)

        response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
      }
    },
  )
