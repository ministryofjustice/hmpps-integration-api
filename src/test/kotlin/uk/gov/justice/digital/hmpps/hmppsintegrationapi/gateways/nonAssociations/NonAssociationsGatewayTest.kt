package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nonAssociations

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NonAssociationsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NonAssociationsApiMockServer

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NonAssociationsGateway::class],
)
class NonAssociationsGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val nonAssociationsGateway: NonAssociationsGateway,
) : DescribeSpec({
    val prisonerNumber = "ASDP211"
    val nonAssociationsApiMockServer = NonAssociationsApiMockServer()

    beforeEach {
      nonAssociationsApiMockServer.start()
      Mockito.reset(hmppsAuthGateway)

      whenever(hmppsAuthGateway.getClientToken("NON-ASSOCIATIONS")).thenReturn(HmppsAuthMockServer.TOKEN)
    }

    afterTest {
      nonAssociationsApiMockServer.stop()
    }

    it("authenticates using HMPPS Auth with credentials") {
      nonAssociationsGateway.getNonAssociationsForPerson(prisonerNumber)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NON-ASSOCIATIONS")
    }

    it("get non associates when quering with a valid hmppsId") {
      nonAssociationsApiMockServer.stubNonAssociationsGet(
        prisonerNumber = prisonerNumber,
        body =
          """
          {
            "prisonerNumber": "A1234BC",
            "firstName": "James",
            "lastName": "Hall",
            "prisonId": "MDI",
            "prisonName": "Moorland (HMP & YOI)",
            "cellLocation": "A-1-002",
            "openCount": 1,
            "closedCount": 0,
            "nonAssociations": [
              {
                "id": 42,
                "role": "VICTIM",
                "roleDescription": "Victim",
                "reason": "BULLYING",
                "reasonDescription": "Bullying",
                "restrictionType": "CELL",
                "restrictionTypeDescription": "Cell only",
                "comment": "John and Luke always end up fighting",
                "authorisedBy": "OFF3_GEN",
                "whenCreated": "2021-12-31T12:34:56.789012",
                "whenUpdated": "2022-01-03T12:34:56.789012",
                "updatedBy": "OFF3_GEN",
                "isClosed": false,
                "closedBy": null,
                "closedReason": null,
                "closedAt": null,
                "otherPrisonerDetails": {
                  "prisonerNumber": "D5678EF",
                  "role": "PERPETRATOR",
                  "roleDescription": "Perpetrator",
                  "firstName": "Joseph",
                  "lastName": "Bloggs",
                  "prisonId": "MDI",
                  "prisonName": "Moorland (HMP & YOI)",
                  "cellLocation": "B-2-007"
                },
                "isOpen": true
              }
           ]
          }
          """.trimIndent(),
      )

      var result = nonAssociationsGateway.getNonAssociationsForPerson(prisonerNumber)
      result.errors.shouldBeEmpty()
      result.data?.shouldBe(1)
      result.data
        ?.prisonId
        .equals("MDI")
    }
  })
