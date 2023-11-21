package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NomisApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import java.time.LocalDate

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NomisGateway::class],
)
class GetLatestSentenceKeyDatesForPersonTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  private val nomisGateway: NomisGateway,
) : DescribeSpec(
  {
    val nomisApiMockServer = NomisApiMockServer()
    val offenderNo = "abc123"

    beforeEach {
      nomisApiMockServer.start()
      nomisApiMockServer.stubGetLatestSentenceKeyDatesForPerson(
        offenderNo,
        """
          {
            "sentenceDetail": {
              "automaticReleaseDate": "2023-03-01",
              "automaticReleaseOverrideDate": "2023-03-01"
            }
          }
        """,
      )

      Mockito.reset(hmppsAuthGateway)
      whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
    }

    afterTest {
      nomisApiMockServer.stop()
    }

    it("authenticates using HMPPS Auth with credentials") {
      nomisGateway.getLatestSentenceKeyDatesForPerson(offenderNo)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
    }

    it("returns latest sentence key dates for a person with the matching ID") {
      val response = nomisGateway.getLatestSentenceKeyDatesForPerson(offenderNo)

      response.data?.automaticRelease?.date.shouldBe(LocalDate.parse("2023-03-01"))
      response.data?.automaticRelease?.overrideDate.shouldBe(LocalDate.parse("2023-03-01"))
      response.data?.automaticRelease?.calculatedDate.shouldBe(null)
    }

    it("returns an error when 404 NOT FOUND is returned") {
      nomisApiMockServer.stubGetLatestSentenceKeyDatesForPerson(
        offenderNo,
        """
        {
          "developerMessage": "cannot find person"
        }
        """,
        HttpStatus.NOT_FOUND,
      )

      val response = nomisGateway.getLatestSentenceKeyDatesForPerson(offenderNo)

      response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
    }
  },
)
