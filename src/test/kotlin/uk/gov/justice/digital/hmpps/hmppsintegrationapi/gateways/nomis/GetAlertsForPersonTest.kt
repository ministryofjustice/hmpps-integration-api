package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
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
class GetAlertsForPersonTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  val nomisGateway: NomisGateway,
) :
  DescribeSpec(
    {
      val nomisApiMockServer = NomisApiMockServer()
      val offenderNo = "zyx987"

      beforeEach {
        nomisApiMockServer.start()
        nomisApiMockServer.stubGetAlertsForPerson(
          offenderNo,
          """
            [
              {
                "alertId": 1,
                "bookingId": 9999111,
                "offenderNo": "A7777ZZ",
                "alertType": "X",
                "alertTypeDescription": "Security",
                "alertCode": "XNR",
                "alertCodeDescription": "Not For Release",
                "comment": "IS91",
                "dateCreated": "2022-08-01",
                "expired": false,
                "active": true,
                "addedByFirstName": "BOB",
                "addedByLastName": "HELMAN"
              }
            ]
          """.removeWhitespaceAndNewlines(),
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        nomisApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        nomisGateway.getAlertsForPerson(offenderNo)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
      }

      it("returns alerts for the matching person ID") {
        val response = nomisGateway.getAlertsForPerson(offenderNo)

        response.data.count().shouldBeGreaterThan(0)
      }

      it("returns a person with an empty list of alerts when no alerts are found") {
        nomisApiMockServer.stubGetAlertsForPerson(offenderNo, "[]")

        val response = nomisGateway.getAlertsForPerson(offenderNo)

        response.data.shouldBeEmpty()
      }

      it("returns an error when 404 Not Found is returned because no person is found") {
        nomisApiMockServer.stubGetAlertsForPerson(offenderNo, "", HttpStatus.NOT_FOUND)

        val response = nomisGateway.getAlertsForPerson(offenderNo)

        response.errors.shouldHaveSize(1)
        response.errors.first().causedBy.shouldBe(UpstreamApi.NOMIS)
        response.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }
    },
  )
